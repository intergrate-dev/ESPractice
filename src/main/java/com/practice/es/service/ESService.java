package com.practice.es.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.bus.bean.DocInfo;
import com.practice.bus.bean.SiteMonitorEntity;
import com.practice.es.core.RestClientTemplate;
import com.practice.util.FastJsonConvertUtil;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.ParsedStats;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

/**
 * 需要提前使用REST AIP创建索引 映射
 * ①发送put 请求到http://192.168.128.135:9200/doc 其中doc为索引名称
 * ②参数为json格式的数据 mapping.json
 * <p>
 * 创建/修改/删除ES Document的类
 */
@Service
public class ESService {
    private static Logger logger = LoggerFactory.getLogger(ESService.class);

    /*@Autowired
    TransportClient transportClient;*/

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private RestClientTemplate template;

    @Autowired
    ObjectMapper objectMapper;

    private final static String INDEX = "doc";
    private final static String TYPE = "office";
    private final static String INDEX_SITE = "sitemonitor";

    /**
     * 添加文档
     *
     * @throws JsonProcessingException
     **/
    public boolean createDocument(ESDocumentTemplate docTemplate) {
        try {
            //将对象转换成json
            /*String json = objectMapper.writeValueAsString(docTemplate);
            logger.info("ES创建文档:" + json);*/

            /*IndexResponse response = transportClient.prepareIndex(INDEX, TYPE, docTemplate.getDocId()).setSource(json, XContentType.JSON).get();
            if (response.status() == RestStatus.CREATED) {
                return true;
            }*/

            IndexRequest indexRequest = new IndexRequest(INDEX);
            //直接使用用户主键作为index的哈哈
            indexRequest.id(docTemplate.getDocId());
            Object o = JSON.toJSON(docTemplate);
            logger.info(o.toString());
            indexRequest.source(o.toString(), XContentType.JSON);
            //同步执行
            IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);

        } catch (Exception e) {
            logger.error("ES添加文档失败", e);
        }
        return false;
    }

    /**
     * 修改文档
     **/
    public boolean modifyDocument(ESDocumentTemplate docTemplate) {
        try {
            //将对象转换成json
            String json = objectMapper.writeValueAsString(docTemplate);
            logger.info("ES修改文档:" + json);

            /*UpdateResponse response = transportClient.prepareUpdate(INDEX, TYPE, docTemplate.getDocId()).setDoc(json, XContentType.JSON).get();
            if (response.status() == RestStatus.OK) {
                return true;
            }*/

            Object o = JSON.toJSON(docTemplate);
            UpdateRequest request = new UpdateRequest(INDEX, docTemplate.getDocId());
            request.doc(o.toString(), XContentType.JSON);

            //可以执行很多可选参数....,这里是个简单示例,就不把官方文档的所有的可选参数都添加上了
            //同步执行
            UpdateResponse updateResponse = client.update(request, RequestOptions.DEFAULT);
            String index = updateResponse.getIndex();
            String id = updateResponse.getId();
            long version = updateResponse.getVersion();

            if (updateResponse.getResult() == DocWriteResponse.Result.CREATED) {
                logger.info("ES返回的updateResponse: {}", updateResponse);
            } else if (updateResponse.getResult() == DocWriteResponse.Result.UPDATED) {
                logger.info("处理文档更新的案例");
            } else if (updateResponse.getResult() == DocWriteResponse.Result.DELETED) {
                logger.info("处理文档被删除的情况");
            } else if (updateResponse.getResult() == DocWriteResponse.Result.NOOP) {
                logger.info("处理文档未受更新影响的情况(未对文档执行任何操作(Noop)。");
            }

        } catch (Exception e) {
            logger.error("ES修改文档失败", e);
        }
        return false;
    }

    /**
     * 删除文档
     **/
    //public boolean deleteDocument(String docId) {
    public boolean deleteDocument(ESDocumentTemplate docTemplate) {
        try {
            //将对象转换成json
            logger.info("ES删除文档:" + docTemplate.getDocId());

            /*DeleteResponse response = transportClient.prepareDelete(INDEX, TYPE, docId).get();
            if (response.status() == RestStatus.OK) {
                return true;
            }*/

            this.delDocByAsy(docTemplate);
        } catch (Exception e) {
            logger.error("ES修改文档失败", e);
        }
        return false;
    }

    public Integer delDocByAsy(ESDocumentTemplate docTemplate) {
        //异步删除
        DeleteRequest request = new DeleteRequest(INDEX, docTemplate.getDocId());
        ActionListener<DeleteResponse> listener = new ActionListener<DeleteResponse>() {

            @Override
            public void onResponse(DeleteResponse deleteResponse) {
                logger.info("异步删除成功(的处理)!");
            }

            @Override
            public void onFailure(Exception e) {
                logger.info("异步删除失败的处理!");
            }
        };
        try {
            client.deleteAsync(request, RequestOptions.DEFAULT, listener);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

    }

    public String queryDocs(Map<String, String> map) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.from(0);
        sourceBuilder.size(10);
        sourceBuilder.fetchSource(new String[]{"doc"}, new String[]{});
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("docId", "002");
        /*TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("tag", "体育");
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("publishTime");
        rangeQueryBuilder.gte("2018-01-26T08:00:00Z");
        rangeQueryBuilder.lte("2018-01-26T20:00:00Z");*/
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
        boolBuilder.must(matchQueryBuilder);
        //boolBuilder.must(termQueryBuilder);
        //boolBuilder.must(rangeQueryBuilder);
        sourceBuilder.query(boolBuilder);
        SearchRequest searchRequest = new SearchRequest(INDEX);
        searchRequest.searchType(TYPE);
        searchRequest.source(sourceBuilder);
        try {
            SearchResponse response = client.search(searchRequest, null);
            logger.info("=============================== es esearch response: {} =======================", response.toString());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public void createSiteInfo(SiteMonitorEntity siteMonitor) {
        IndexRequest indexRequest = new IndexRequest(INDEX_SITE);
        indexRequest.id(SiteMonitorEntity.genRequestId(siteMonitor));
        String source = JSON.toJSON(siteMonitor).toString();
        indexRequest.source(source, XContentType.JSON);
        try {
            IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
            logger.info("================= createSiteInfo, indexResponse status: {}, source: {}", indexResponse.status(), source);
        } catch (IOException e) {
            logger.error("================= createSiteInfo error, id: {}, error: {} =============", indexRequest.id(), e.getMessage());
            e.printStackTrace();
        }
    }

    public void modifySiteInfo(SiteMonitorEntity siteMonitor) {
        String source = JSON.toJSON(siteMonitor).toString();
        UpdateRequest request = new UpdateRequest(INDEX_SITE, SiteMonitorEntity.genRequestId(siteMonitor));
        request.doc(source, XContentType.JSON);
        try {
            UpdateResponse updateResponse = client.update(request, RequestOptions.DEFAULT);
            logger.info("================= createSiteInfo, indexResponse status: {}, source: {}", updateResponse.status(), source);
        } catch (IOException e) {
            logger.error("================= createSiteInfo error, id: {}, error: {} =============", request.id(), e.getMessage());
            e.printStackTrace();
        }
    }

    public Map<String, Object> querySiteInfo(Integer pageNo, Integer limit) {
        Map<String, Object> resMap = new HashMap<>();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        Integer startIndex = (pageNo - 1) * limit;
        sourceBuilder.from(startIndex);
        sourceBuilder.size(startIndex + limit);
        String[] includes = new String[]{"id", "siteName", "task", "status", "dataChannel", "createTime", "updateTime"};
        sourceBuilder.fetchSource(includes, new String[]{});

        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
        sourceBuilder.query(boolBuilder)
                .aggregation(AggregationBuilders.terms("aggreofid").field("id")
                        .subAggregation(AggregationBuilders.stats("aggreofstatus").field("status"))
                        //.subAggregation(AggregationBuilders.cardinality("statusaggre").field("status"))
                )
                //.aggregation(AggregationBuilders.terms("aggreofstatus").field("status"))
                //.aggregation(AggregationBuilders.stats("aggreofstatus").field("status"))
                //.size(0)
                .sort("updateTime", SortOrder.DESC);

        // TODO 按站点状态聚合查询
        SearchRequest searchRequest = new SearchRequest(INDEX_SITE);
        searchRequest.source(sourceBuilder);
        SearchResponse response = null;
        try {
            response = client.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits searchHits = response.getHits();
            SearchHit[] hits = searchHits.getHits();
            List<SiteMonitorEntity> list = new ArrayList<>();
            Map<String, SiteMonitorEntity> map = new HashMap<>();
            Arrays.stream(hits).forEach(hit -> {
                SiteMonitorEntity sme = FastJsonConvertUtil.convertJSONToObject(hit.getSourceAsString(), SiteMonitorEntity.class);
                if (!map.containsKey(sme.getId())) {
                    map.put(sme.getId(), sme);
                    list.add(sme);
                }
            });

            List<Aggregation> aggregations = response.getAggregations().asList();
            List<? extends Terms.Bucket> buckets = ((ParsedStringTerms) aggregations.get(0)).getBuckets();
            JSONArray arrayF = new JSONArray();
            JSONArray arrayB = new JSONArray();
            buckets.stream().forEach(b -> {
                Object key = ((Terms.Bucket) b).getKey();
                List<Aggregation> aggrs = ((Terms.Bucket) b).getAggregations().asList();
                Aggregation aggregation = aggrs.get(0);
                JSONObject json = new JSONObject();
                if (map.containsKey(key)) {
                    json.put("siteId", map.get(key).getId());
                    json.put("siteName", map.get(key).getSiteName());
                    json.put("channel", map.get(key).getDataChannel());
                    json.put("createTime", map.get(key).getCreateTime());
                    json.put("updateTime", map.get(key).getUpdateTime());
                }
                if (((ParsedStats) aggregation).getAvg() == 1L) {
                    json.put("status", "1");
                    arrayF.add(json);
                } else {
                    json.put("status", "0");
                    arrayB.add(json);
                }
            });
            arrayF.addAll(arrayB);
            long total = searchHits.getTotalHits().value;
            resMap.put("totalCount", total);
            resMap.put("totalPage", Math.round((Math.ceil(total * 1.0 / limit))));
            resMap.put("list", arrayF);
            logger.info("============================== es esearch response: {} =======================", response.toString());
        } catch (IOException e) {
            logger.error("============================= es esearch occure error, message: {} =======================", e.getMessage());
            e.printStackTrace();
        }

        return resMap;
    }

    public void mappingSiteInfo(JSONObject json) {
        /*HttpEntity entity = new NStringEntity(json.toString(), ContentType.APPLICATION_JSON);
        // 使用RestClient进行操作 而非rhlClient
        RetentionLeaseActions.Response response = client.performRequest("put", "/demo", Collections.<String, String> emptyMap(),
                entity);
        System.out.println(response);*/

        /*Map<String, Object> properties = new HashMap<>();
        properties.put("name", null);
        properties.put("age", null);
        properties.put("address", null);*/

        Map<String, Object> parseMap = FastJsonConvertUtil.json2Map(json.toString());
        /*Map<String, Object> mapping = new HashMap<>();
        mapping.put("properties", parseMap.get("mappings"));

        Map<String, Object> settings = new HashMap<>();
        mapping.put("properties", json.getString("settings"));*/

        CreateIndexRequest request = new CreateIndexRequest(INDEX_SITE).mapping((Map<String, ?>) parseMap.get("mappings"))
                .settings((Map<String, ?>) parseMap.get("settings"));
        template.opsForIndices().create(request);
    }

    public void deleteSiteInfo(SiteMonitorEntity siteMonitor) {
        DeleteRequest request = new DeleteRequest(INDEX_SITE, SiteMonitorEntity.genRequestId(siteMonitor));
        ActionListener<DeleteResponse> listener = new ActionListener<DeleteResponse>() {

            @Override
            public void onResponse(DeleteResponse deleteResponse) {
                logger.info("异步删除成功(的处理)!");
            }

            @Override
            public void onFailure(Exception e) {
                logger.info("异步删除失败的处理!");
            }
        };
        try {
            client.deleteAsync(request, RequestOptions.DEFAULT, listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
