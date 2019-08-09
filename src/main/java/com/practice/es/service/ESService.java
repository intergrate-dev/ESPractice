package com.practice.es.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.frameworkset.util.RegexUtil;
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
import org.elasticsearch.index.reindex.*;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.ParsedStats;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.elasticsearch.client.ClientInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

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
        try {
            MatchQueryBuilder builder_1 = QueryBuilders.matchQuery("id", siteMonitor.getId());
            MatchQueryBuilder builder_2 = QueryBuilders.matchQuery("task", siteMonitor.getTask());
            BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
            boolBuilder.must(builder_1).must(builder_2);
            Script script = new Script("ctx._source.status = ".concat(siteMonitor.getStatus()));
            UpdateByQueryRequest request = new UpdateByQueryRequest(INDEX_SITE).setQuery(boolBuilder).setScript(script);
            request.setConflicts("proceed");
            BulkByScrollResponse response = client.updateByQuery(request, RequestOptions.DEFAULT);
            logger.info("================= modifySiteInfo, update item: {}, source: {}", response.getStatus().getTotal(), source);
        } catch (IOException e) {
            logger.error("================= modifySiteInfo error, id: {}, error: {} =============", siteMonitor.getId(), e.getMessage());
            e.printStackTrace();
        }
    }

    public Map<String, Object> querySiteInfo(Integer pageNo, Integer limit) {
        Map<String, Object> resMap = new HashMap<>();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        Integer from = 0;
        Integer offSize = 5000;
        sourceBuilder.from(from);
        sourceBuilder.size(offSize);
        String[] includes = new String[]{"id", "siteName", "task", "status", "dataChannel", "createTime", "updateTime"};
        sourceBuilder.fetchSource(includes, new String[]{});

        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
        sourceBuilder.query(boolBuilder)
                .aggregation(AggregationBuilders.terms("aggreofid").field("id").size(offSize)
                                .subAggregation(AggregationBuilders.stats("aggreofstatus").field("status"))
                        //.subAggregation(AggregationBuilders.cardinality("statusaggre").field("status"))
                )
                //.aggregation(AggregationBuilders.terms("aggreofstatus").field("status"))
                //.size(100)
                .sort("status", SortOrder.ASC);

        // TODO 按站点状态聚合查询
        SearchRequest searchRequest = new SearchRequest(INDEX_SITE);
        searchRequest.source(sourceBuilder);
        SearchResponse response = null;
        try {
            response = client.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits searchHits = response.getHits();
            SearchHit[] hits = searchHits.getHits();
            Map<String, SiteMonitorEntity> map = new HashMap<>();
            Arrays.stream(hits).forEach(hit -> {
                SiteMonitorEntity sme = FastJsonConvertUtil.convertJSONToObject(hit.getSourceAsString(), SiteMonitorEntity.class);
                if (!map.containsKey(sme.getId())) {
                    //map.put(sme.getId(), sme);
                    map.put(sme.getId().toLowerCase(), sme);
                }
            });
            List<Aggregation> aggregations = response.getAggregations().asList();
            List<? extends Terms.Bucket> buckets = ((ParsedStringTerms) aggregations.get(0)).getBuckets();
            JSONArray arrayFir = new JSONArray();
            JSONArray arraySec = new JSONArray();
            JSONArray arrayThir = new JSONArray();
            buckets.stream().forEach(b -> {
                String key = (String) ((Terms.Bucket) b).getKey();
                if (RegexUtil.isMatch(key, ".*[a-zA-Z]+.*")) {
                    //key = key.toUpperCase().concat("==");
                    key = key.concat("==");
                }
                String keyCp = "-".concat((String) key);
                List<Aggregation> aggrs = ((Terms.Bucket) b).getAggregations().asList();
                if (aggrs != null && aggrs.size() > 0) {
                    Aggregation aggregation = aggrs.get(0);
                    SiteMonitorEntity entity = map.get(key) == null ? map.get(keyCp) : map.get(key);
                    if (entity == null) {
                        logger.info("========================= entity empty key: {} ===================", key);
                    }
                    if (map.containsKey(key) || map.containsKey(keyCp)) {
                        JSONObject json = new JSONObject();
                        json.put("id", entity.getId());
                        json.put("siteName", entity.getSiteName());
                        json.put("dataChannel", entity.getDataChannel());
                        json.put("createTime", entity.getCreateTime());
                        json.put("updateTime", entity.getUpdateTime());
                        if (aggregation instanceof ParsedStats) {
                            ParsedStats at = (ParsedStats) aggregation;
                            if (at.getMin() == -1L) {
                                json.put("status", "-1");
                                arrayFir.add(json);
                            } else {
                                if (at.getAvg() == 1L) {
                                    json.put("status", "1");
                                    arrayThir.add(json);
                                } else {
                                    json.put("status", "0");
                                    arraySec.add(json);
                                }
                            }
                        }
                        json.put("extInfo", entity.getExtInfo());
                    }
                }
            });
            arraySec.addAll(arrayThir);
            arrayFir.addAll(arraySec);
            //按分页取
            Integer total = arrayFir.size();
            Integer startIndx = (pageNo - 1) * limit;
            Integer endIndx = startIndx + limit > total ? total : startIndx + limit;
            List<Object> resList = arrayFir.subList(startIndx, endIndx);
            resMap.put("totalCount", total);
            resMap.put("totalPage", Math.round((Math.ceil(total * 1.0 / limit))));
            resMap.put("list", resList);
            //logger.info("============================== es esearch response: {} =======================", response.toString());
        } catch (IOException e) {
            logger.error("============================= es esearch occure IOException, message: {} =======================", e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("============================= es esearch occure Exception, message: {} =======================", e.getMessage());
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
