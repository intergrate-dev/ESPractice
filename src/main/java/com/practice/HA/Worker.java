package com.practice.HA;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.common.Constant;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Worker {

    private static final Logger logger = LoggerFactory.getLogger(Worker.class);
    private static JdbcTemplate jdbcTemplate;
    private final ObjectMapper mapper = new ObjectMapper();
    private ZKConnector zkClient =null;
    private TransportClient client =null;
    private Timestamp currentTimestamp = null;
    private Timestamp previousTimestamp = null;
    private static final String oggSql = "select * from t_order t0 left join t_order_attachedinfo t1 on t0.order_id = t1.order_id where ";

    private String sql;

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    private TransportClient getClient() {
        Settings settings = Settings.settingsBuilder().put("cluster.name", Constant.CLUSTER).build();
        TransportClient client = TransportClient.builder().settings(settings).build();
        try {
            client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(Constant.ESHOST), Constant.ESPORT));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return client;
    }

    public Worker(AbstractApplicationContext ctx){
        //初始化Oracle连接
        jdbcTemplate = (JdbcTemplate) ctx.getBean("jdbcTemplate");
        client = getClient();
        zkClient = new ZKConnector();
        zkClient.createConnection(Constant.ZKSERVER, Constant.SESSION_TIMEOUT);

        //初始化zookeeper锁,由于zookeeper不能联级创建
        if(!zkClient.exist(Constant.ZK_PATH)){
            zkClient.createPersistNode(Constant.ZK_PATH,"");
        }

        /**
         * 获取zookeeper的最后同步时间
         */
        if(currentTimestamp == null){
            String zkTimestamp = zkClient.readData(Constant.NODE_PATH);
            if(zkTimestamp != null && !zkTimestamp.equals(""))
            {
                try
                {
                    currentTimestamp = Timestamp.valueOf(zkTimestamp);
                    logger.info("获取zookeeper最后同步时间： "+currentTimestamp);
                }catch(Exception e){
                    zkClient.deleteNode(Constant.NODE_PATH);
                }
            }
        }
    }

    /**
     * 同步work的逻辑：
     *     将Oracle里面的规则表同步到缓存当中
     *     首先是访问Oracle里面数据，通过访问最小锁里面的同步时间戳，查询出大于同步时间戳的数据
     *  如果在zookeeper中获取的时间戳为空，则查询条件增加时间戳，写入存储框架
     *  写入成功之后，将最后一条记录的同步时间戳写到zookeeper集群中
     *  若写入失败，和zookeeper握手失败，会话锁消失
     *  然后导入ElasticSearch中
     */
    public void doWork(){
        logger.info("start ...");
        //一直进行同步工作
        while(true){
            String sqlwhere = "";
            //根据时间戳获取Mycat中规则表数据
            String sql = "";
            //若最后一次同步时间为空，则按最后更新时间排序，取最小的时间作为当前时间戳
            if(currentTimestamp != null){
                sql = "select order_id,timestamp from t_order_changes  where rownum <= 10 and timestamp > to_timestamp('" + currentTimestamp.toString() + "','yyyy-mm-dd hh24:mi:ss.ff6')";
            }else{
                sql = "select order_id,timestamp from t_order_changes  where rownum <= 10 order by timestamp";
            }

            //查詢该时间段的订单id
            List<String> ids = new ArrayList<String>();

            //升序会将最后一次的时间也就是最大的时间作为当前的currentTimeStamp
            ids = jdbcTemplate.query(sql, new Object[] {}, new RowMapper<String>()
            {
                public String mapRow(ResultSet result, int rowNum) throws SQLException {
                    currentTimestamp = result.getTimestamp("timestamp");
                    return result.getString("order_id");
                }
            });

            if(ids.size() ==0){
                continue;
            }

            int i =0;
            List<String> checkIds = new ArrayList<String>();
            for (String id : ids) {
                //若存在更新的id则跳过
                if (checkIds.contains(id)) {
                    continue;
                }
                if (i == 0) {
                    sqlwhere = sqlwhere.concat(" t0.order_id = '" + id + "'");
                } else {
                    sqlwhere = sqlwhere.concat(" or t0.order_id = '" + id + "'");
                }
                checkIds.add(id);
                i++;
            }

            System.out.println(oggSql.concat(sqlwhere));
            //objs 即是Oracle里面查询出来需要同步的数据
            List<JSONObject> objs = jdbcTemplate.query(oggSql.concat(sqlwhere), new Object[] {}, new RowMapper<JSONObject>()
            {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                public JSONObject mapRow(ResultSet result, int rowNum) throws SQLException {
                    int c = result.getMetaData().getColumnCount();
                    JSONObject obj = new JSONObject();

                    for(int t =1 ;t <= c;t++)
                    {
                        if(result.getObject(t) == null)
                        {
                            continue;
                        }
                        if(result.getMetaData().getColumnType(t) == Types.DATE)
                        {
                            obj.put(result.getMetaData().getColumnLabel(t).toLowerCase(), result.getDate(t));
                        }else if(result.getMetaData().getColumnType(t) == Types.TIMESTAMP)
                        {
                            Date date = new Date(result.getTimestamp(t).getTime());
                            String f = sdf.format(date);
                            obj.put(result.getMetaData().getColumnLabel(t).toLowerCase(),sdf.format(date));
                        }else
                        {
                            obj.put(result.getMetaData().getColumnLabel(t).toLowerCase(), result.getObject(t));
                        }
                    }
                    return obj;
                }
            });

            /*for (JSONObject obj : objs) {
                System.out.println(obj.toJSONString());
            }*/

            /**
             * 将查询出来的数据写入到elasticsearch中
             */
            BulkRequestBuilder bulkRequest =null;
            try {
                bulkRequest = client.prepareBulk();

                for (JSONObject obj : objs) {
                    byte[] json;

                    try {
                        json = mapper.writeValueAsBytes(obj);
                        bulkRequest.add(new IndexRequest(Constant.INDEX, Constant.INDEX, obj.getString("order_id"))
                                .source(json));

                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }

                BulkResponse bulkResponse = bulkRequest.get();

                if (bulkResponse.hasFailures()) {
                    logger.info("====================批量创建索引过程中出现错误 下面是错误信息==========================");
                    long count = 0L;
                    for (BulkItemResponse bulkItemResponse : bulkResponse) {
                        System.out.println("发生错误的 索引id为 : "+bulkItemResponse.getId()+" ，错误信息为："+ bulkItemResponse.getFailureMessage());
                        count++;
                    }
                    logger.info("====================批量创建索引过程中出现错误 上面是错误信息 共有: "+count+" 条记录==========================");
                    currentTimestamp = previousTimestamp;
                } else {
                    logger.info("The lastest currenttimestamp : ".concat(currentTimestamp.toString()));
                    previousTimestamp = currentTimestamp;
                    //将写入成功后的时间写到zookeeper中
                    zkClient.writeData(Constant.NODE_PATH, String.valueOf(currentTimestamp));
                }

            } catch (NoNodeAvailableException e) {
                currentTimestamp = previousTimestamp;
                e.printStackTrace();
            }
        }

    }


}
