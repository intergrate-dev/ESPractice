package com.practice.es.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.frameworkset.util.RegexUtil;
import com.practice.bus.bean.DocInfo;
import com.practice.bus.bean.MediaArticle;
import com.practice.bus.bean.SiteMonitorEntity;
import com.practice.bus.bean.vo.MediaArtiStatsVo;
import com.practice.es.core.RestClientTemplate;
import com.practice.util.DateParseUtil;
import com.practice.util.FastJsonConvertUtil;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
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
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.histogram.ParsedDateHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.ParsedStats;
import org.elasticsearch.search.aggregations.metrics.ParsedSum;
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

    /*@Autowired
    private RestHighLevelClient client;*/

    @Autowired
    private RestClientTemplate template;

    @Autowired
    ObjectMapper objectMapper;

    private final static String INDEX = "doc";
    private final static String TYPE = "office";
    private final static String INDEX_SITE = "sitemonitor";
    private final static String INDEX_ARTICLE = "media_article";

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
            IndexResponse indexResponse = template.getClient().index(indexRequest, RequestOptions.DEFAULT);

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
            UpdateResponse updateResponse = template.getClient().update(request, RequestOptions.DEFAULT);
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
            template.getClient().deleteAsync(request, RequestOptions.DEFAULT, listener);
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
            SearchResponse response = template.getClient().search(searchRequest, null);
            logger.info("---------------------------- es esearch response: {} ---------------------------", response.toString());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public void createSiteInfo(SiteMonitorEntity entity) {
        String rId = SiteMonitorEntity.genRequestId(entity);
        String index = INDEX_SITE;
        this.createEsEntity(entity, rId, index);
    }

    private void createEsEntity(Object entity, String rId, String index) {
        if (entity == null || StringUtils.isEmpty(rId) || StringUtils.isEmpty(index)) {
            logger.error("========================== createEsEntity occure error, message: entiry , rId or index exist empty value =====================");
            return;
        }
        IndexRequest indexRequest = new IndexRequest(index).id(rId);
        indexRequest.source(JSON.toJSON(entity).toString(), XContentType.JSON);
        try {
            IndexResponse indexResponse = template.getClient().index(indexRequest, RequestOptions.DEFAULT);
            logger.info("------------------ createSiteInfo, rId: {}, indexResponse status: {}, source: {} ----------------", rId, indexResponse.status(), indexRequest.source());
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
            BulkByScrollResponse response = template.getClient().updateByQuery(request, RequestOptions.DEFAULT);
            logger.info("------------------------------ modifySiteInfo, update item: {}, source: {} ------------------------", response.getStatus().getTotal(), source);
        } catch (IOException e) {
            logger.error("================= modifySiteInfo error, id: {}, error: {} =============", siteMonitor.getId(), e.getMessage());
            e.printStackTrace();
        }
    }

    public Map<String, Object> querySiteInfo(Integer pageNo, Integer limit) throws Exception {
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
                )
                //.size(100)
                .sort("status", SortOrder.ASC);

        SearchRequest searchRequest = new SearchRequest(INDEX_SITE);
        searchRequest.source(sourceBuilder);
        SearchResponse response = null;
        response = template.getClient().search(searchRequest, RequestOptions.DEFAULT);
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
                /*if (entity == null) {
                    logger.info("-------------------------- entity empty key: {} -----------------------------", key);
                }*/
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
        //logger.info("------------------------- es esearch response: {} ------------------------------", response.toString());
        return resMap;
    }

    public void mappingSiteInfo(JSONObject json) {
        Map<String, Object> parseMap = FastJsonConvertUtil.json2Map(json.toString());
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
            template.getClient().deleteAsync(request, RequestOptions.DEFAULT, listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JSONArray queryAggsByStatus() throws Exception {
        JSONArray resArr = new JSONArray();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
        sourceBuilder.query(boolBuilder).aggregation(AggregationBuilders.terms("agg_status").field("status"));

        List<Aggregation> aggregations = getAggregations(sourceBuilder, INDEX_SITE);
        if (aggregations.size() == 0) {
            return null;
        }
        List<? extends Terms.Bucket> buckets = ((ParsedLongTerms) aggregations.get(0)).getBuckets();
        buckets.stream().forEach(b -> {
            JSONObject json = new JSONObject();
            json.put("status", ((Terms.Bucket) b).getKey());
            json.put("docCount", ((Terms.Bucket) b).getDocCount());
            resArr.add(json);
        });
        return resArr;
    }

    public void createMediaArticle(MediaArticle entity) {
        String rId = MediaArticle.genRequestId(entity);
        this.createEsEntity(entity, rId, INDEX_ARTICLE);
    }

    public Map<String, Object> querayMediaStats(String mediaId, Integer integer) throws IOException {
        Map<String, Object> resMap = new HashMap<>();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        Integer from = 0;
        Integer offSize = 10000;
        sourceBuilder.from(from);
        sourceBuilder.size(offSize);
        String[] includes = new String[]{"id", "name", "mediaId", "dataType", "pubdate", "location", "weekOfYear"};
        sourceBuilder.fetchSource(includes, new String[]{});
        TermQueryBuilder termByMedia = QueryBuilders.termQuery("mediaId", mediaId);
        TermQueryBuilder termByWeek = QueryBuilders.termQuery("weekOfYear", DateParseUtil.queryTodayWeekOfYear(new Date()));

        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
        boolBuilder.must(termByMedia);
        boolBuilder.must(termByWeek);
        sourceBuilder.query(boolBuilder)
                .aggregation(AggregationBuilders.terms("aggofname").field("name").size(offSize)
                        .subAggregation(AggregationBuilders.dateHistogram("dataShow").field("pubdate").calendarInterval(DateHistogramInterval.DAY)
                                .format(DateParseUtil.DATE_STRICK)
                                .subAggregation(AggregationBuilders.sum("visitTotal").field("visitCount"))
                                .subAggregation(AggregationBuilders.sum("likeTotal").field("likeCount"))
                        )
                ).sort("pubdate", SortOrder.ASC);

        List<Aggregation> aggregations = this.getAggregations(sourceBuilder, INDEX_ARTICLE);
        List<? extends Terms.Bucket> buckets = ((ParsedStringTerms) aggregations.get(0)).getBuckets();
        List<MediaArtiStatsVo> masvList = new ArrayList<>();
        buckets.stream().forEach(b -> {
            ParsedStringTerms.ParsedBucket pst = (ParsedStringTerms.ParsedBucket) b;
            List<? extends Histogram.Bucket> hgs = ((ParsedDateHistogram) pst.getAggregations().asList().get(0)).getBuckets();
            MediaArtiStatsVo masv = new MediaArtiStatsVo();
            masv.setSourceName(pst.getKeyAsString());

            List<String> publish = new ArrayList<>();
            List<String> scan = new ArrayList<>();
            List<String> like = new ArrayList<>();
            List<String> hgIncludeKeys = new ArrayList<>();
            hgs.stream().forEach(hg -> {
                hgIncludeKeys.add(((ParsedDateHistogram.ParsedBucket) hg).getKeyAsString());
                publish.add(this.filtPostfix(String.valueOf(hg.getDocCount())));
                List<Aggregation> hists = ((ParsedDateHistogram.ParsedBucket) hg).getAggregations().asList();
                hists.stream().forEach(cv -> {
                    ParsedSum ps = (ParsedSum) cv;
                    if (ps.getName().equals(MediaArtiStatsVo.TOTAL_VISIT)) {
                        scan.add(this.filtPostfix(ps.getValueAsString()));
                    }
                    if (ps.getName().equals(MediaArtiStatsVo.TOTAL_LIKE)) {
                        like.add(this.filtPostfix(ps.getValueAsString()));
                    }
                });
            });

            List<String> days = DateParseUtil.datesOfLastWeek();
            if (hgIncludeKeys.size() < days.size()) {
                for (String day : days) {
                    if (hgIncludeKeys.contains(day)) {
                        continue;
                    }
                    publish.add(days.indexOf(day), "0");
                    scan.add(days.indexOf(day), "0");
                    like.add(days.indexOf(day), "0");
                }
            }
            masv.setPublish(publish);
            masv.setScan(scan);
            masv.setLike(like);
            masvList.add(masv);
        });
        resMap.put("total", masvList.size());
        resMap.put("rows", masvList);
        //logger.info("------------------------- es esearch response: {} ------------------------------", response.toString());
        return resMap;
    }

    private List<Aggregation> getAggregations(SearchSourceBuilder sourceBuilder, String indexArticle) throws IOException {
        SearchRequest searchRequest = new SearchRequest(indexArticle);
        searchRequest.source(sourceBuilder);
        SearchResponse response = null;
        response = template.getClient().search(searchRequest, RequestOptions.DEFAULT);

        return response.getAggregations().asList();
    }

    private String filtPostfix(String source) {
        if (StringUtils.isEmpty(source)) {
            return null;
        }
        if (source.contains(".")) {
            return source.substring(0, source.indexOf("."));
        }
        return source;
    }

    public void bulkPutIndex(List<MediaArticle> mas) {
        BulkRequest request = new BulkRequest();
        for (MediaArticle ma : mas) {
            String rId = MediaArticle.genRequestId(ma);
            IndexRequest indexRequest = new IndexRequest(INDEX_ARTICLE).id(rId);
            indexRequest.source(JSON.toJSON(ma).toString(), XContentType.JSON);
            request.add(indexRequest);
        }
        BulkResponse bulk = null;
        try {
            bulk = template.getClient().bulk(request, RequestOptions.DEFAULT);
            logger.info("------------------------ bulkPutIndex, status: {} --------------------", bulk.status());
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("========================== bulkPutIndex occure error, message: {} =====================", bulk.buildFailureMessage());
        }
    }
}
