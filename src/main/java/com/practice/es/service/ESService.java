package com.practice.es.service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.practice.bus.bean.SiteMonitorEntity;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.bus.bean.DocInfo;

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
    ObjectMapper objectMapper;

    private final static String INDEX = "doc";
    private final static String TYPE = "office";
    private final static String INDEX_SITE = "siteMonitor";

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
        searchRequest.types(TYPE);
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
        indexRequest.id(siteMonitor.getId().concat(siteMonitor.getTask()));
        String source = JSON.toJSON(siteMonitor).toString();
        indexRequest.source(source, XContentType.JSON);
        try {
            IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
            logger.error("================= createSiteInfo, indexResponse status: {}, source: {}", indexResponse.status(), source);
        } catch (IOException e) {
            logger.error("================= createSiteInfo error, id: {}, error: {} =============", indexRequest.id(), e.getMessage());
            e.printStackTrace();
        }
    }

    public void modifySiteInfo(SiteMonitorEntity siteMonitor) {
        String source = JSON.toJSON(siteMonitor).toString();
        UpdateRequest request = new UpdateRequest(INDEX_SITE, siteMonitor.getId().concat(siteMonitor.getTask()));
        request.doc(source, XContentType.JSON);
        try {
            UpdateResponse updateResponse = client.update(request, RequestOptions.DEFAULT);
            logger.error("================= createSiteInfo, indexResponse status: {}, source: {}", updateResponse.status(), source);
        } catch (IOException e) {
            logger.error("================= createSiteInfo error, id: {}, error: {} =============", request.id(), e.getMessage());
            e.printStackTrace();
        }
    }

    public void querySiteInfo() {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.from(0);
        sourceBuilder.size(10);
        sourceBuilder.fetchSource(new String[]{INDEX_SITE}, new String[]{});
        //MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("docId", "002");
        /*TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("tag", "体育");
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("publishTime");
        rangeQueryBuilder.gte("2018-01-26T08:00:00Z");
        rangeQueryBuilder.lte("2018-01-26T20:00:00Z");*/
        // TODO search (包含siteId, all status: 1 >> 1 : 0)
        // TODO order by status desc and updateTime desc
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
        //boolBuilder.must(matchQueryBuilder);
        //boolBuilder.must(termQueryBuilder);
        //boolBuilder.must(rangeQueryBuilder);
        sourceBuilder.query(boolBuilder);
        sourceBuilder.sort("updateTime", SortOrder.DESC);
        // TODO 按站点状态聚合查询
        SearchRequest searchRequest = new SearchRequest(INDEX_SITE);
        searchRequest.source(sourceBuilder);
        try {
            SearchResponse response = client.search(searchRequest, null);

            logger.info("=============================== es esearch response: {} =======================", response.toString());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 根据用户输入条件进行查询
     *
     * @throws ParseException
     **/
    public ESSearchResp<DocInfo> query(ESSearchReq esSearchReq) throws ParseException {
        /*BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //针对keyword进行过滤
        if (!StringUtils.isEmpty(esSearchReq.getDocType())) {
            boolQuery.filter(QueryBuilders.termQuery(ESFieldName.DOC_TYPE, esSearchReq.getDocType()));
        }
        if (!StringUtils.isEmpty(esSearchReq.getAuthor())) {
            boolQuery.filter(QueryBuilders.termQuery(ESFieldName.AUTHOR, esSearchReq.getAuthor()));
        }

        //针对text进行全文检索 查询docName OR docSummary满足匹配要求的结果
        if (!StringUtils.isEmpty(esSearchReq.getInputKeyword())) {
            boolQuery.must(QueryBuilders.multiMatchQuery(esSearchReq.getInputKeyword(), ESFieldName.DOC_NAME, ESFieldName.DOC_SUMMARY));
        }

        //针对时间进行范围查询
        RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery(ESFieldName.CREATE_TIME);
        if (esSearchReq.getBeginTime() != null) {
            boolQuery.filter(rangeQuery.format("yyyy-MM-dd HH:mm:ss").gte(esSearchReq.getBeginTime()));
        }
        if (esSearchReq.getEndTime() != null) {
            boolQuery.filter(rangeQuery.format("yyyy-MM-dd HH:mm:ss").lte(esSearchReq.getEndTime()));
        }

        //针对数组查询
        if (esSearchReq.getTags().size() > 0) {
            boolQuery.filter(QueryBuilders.termsQuery(ESFieldName.TAGS, esSearchReq.getTags()));
        }

        //高亮显示
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<strong>");
        highlightBuilder.postTags("</strong>");
        highlightBuilder.field(ESFieldName.DOC_NAME).field(ESFieldName.DOC_SUMMARY);

        //排序 分页
        SearchRequestBuilder searchRequestBuilder = transportClient.prepareSearch(INDEX).setTypes(TYPE).setQuery(boolQuery)
                //.addSort(ESFieldName.CREATE_TIME,SortOrder.DESC)//默认是按照相关性的得分来排序的
                .setFrom(esSearchReq.getFrom())
                .setSize(esSearchReq.getSize())
                .highlighter(highlightBuilder);//对匹配内容高亮显示
        //.setFetchSource(ESFieldName.DOC_ID, null);//可以设置es查询返回指定字段，默认为全部返回

        logger.info(searchRequestBuilder.toString());

        SearchResponse searchResponse = searchRequestBuilder.get();
        List<DocInfo> result = new ArrayList<>();

        if (searchResponse.status() != RestStatus.OK) {
            logger.error("测试boolean查询失败：" + searchRequestBuilder);
            return new ESSearchResp<DocInfo>(0, result);
        } else {
			for (SearchHit hit : searchResponse.getHits()) {
				DocInfo docInfo = new DocInfo();
				docInfo.setDocId(Long.parseLong((String)hit.getSource().get(ESFieldName.DOC_ID)));
				Map<String, HighlightField> highlightFields = hit.getHighlightFields();
				//如果有高亮信息，用高亮信息 这样前端取出相应值就能直接在html中展示了
				if(highlightFields.get(ESFieldName.DOC_NAME)!=null) {
					docInfo.setDocName(highlightFields.get(ESFieldName.DOC_NAME).getFragments()[0].string());
				}else {
					docInfo.setDocName((String)hit.getSource().get(ESFieldName.DOC_NAME));	
				}
				if(highlightFields.get(ESFieldName.DOC_SUMMARY)!=null) {
					docInfo.setDocSummary(highlightFields.get(ESFieldName.DOC_SUMMARY).getFragments()[0].string());
				}else {
					docInfo.setDocSummary((String)hit.getSource().get(ESFieldName.DOC_SUMMARY));	
				}
				docInfo.setAuthor((String)hit.getSource().get(ESFieldName.AUTHOR));
				docInfo.setCreateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse((String)hit.getSource().get(ESFieldName.CREATE_TIME)));
				result.add(docInfo);
			}
            return new ESSearchResp<DocInfo>(searchResponse.getHits().getTotalHits(), result);
        }*/
        return null;
    }

    /**
     * 聚合查询
     */
    public long aggregationQuery(String author) {
        /*BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (!StringUtils.isEmpty(author)) {
            boolQuery.filter(QueryBuilders.termQuery(ESFieldName.AUTHOR, author));
        }
        SearchRequestBuilder searchRequestBuilder = transportClient.prepareSearch(INDEX).setTypes(TYPE)
                .setQuery(boolQuery)
                .addAggregation(AggregationBuilders.terms(ESFieldName.AGGREGATION_OF_AUTHOR)//聚合结果对应的名称
                        .field(ESFieldName.AUTHOR))//对那个字段执行聚合 类似group by后面的字段
                .setSize(0);//表示只要聚合结果，不要其他具体的数据（作者 文档名 时间等等）

        logger.info(searchRequestBuilder.toString());

        SearchResponse searchResponse = searchRequestBuilder.get();

        if (searchResponse.status() != RestStatus.OK) {
            logger.error("测试聚合查询失败：" + searchRequestBuilder);
        } else {
            Terms terms = searchResponse.getAggregations().get(ESFieldName.AGGREGATION_OF_AUTHOR);
            if (terms.getBuckets() != null && !terms.getBuckets().isEmpty()) {
                //这里查出指定作者一共有多少篇文章
                return terms.getBucketByKey(author).getDocCount();
            }
        }*/
        return 0L;
    }

}
