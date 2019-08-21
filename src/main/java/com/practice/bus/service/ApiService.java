package com.practice.bus.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.practice.bus.bean.MediaArticle;
import com.practice.bus.bean.MediaSource;
import com.practice.common.SystemConstant;
import com.practice.common.http.HttpAPIService;
import com.practice.common.redis.RedisService;
import com.practice.common.token.TokenManager;
import com.practice.config.BigScreenConfig;
import com.practice.es.service.ESService;
import com.practice.util.CommonUtil;
import com.practice.util.DateParseUtil;
import com.practice.util.FastJsonConvertUtil;
import com.practice.util.JsonUtil;
import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.frameworkset.spi.async.annotation.Async;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

@Service
public class ApiService {

    private static Logger logger = LoggerFactory.getLogger(ApiService.class);

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    ESService esService;

    @Autowired
    HttpAPIService httpAPIService;

    @Autowired
    TokenManager tokenManager;

    @Autowired
    RedisService redisService;

    @Autowired
    BigScreenConfig bigScreenConfig;

    public Map<String, Object> queryMediaSrouce(Integer pageNo, Integer limit, List<String> types, String mediaId) {
        /*String url = "/api/source";
        Map<String, String> map = new HashMap<>();
        map.put("type", CommonUtil.listToString(types));
        map.put("mediaId", mediaId);
        map.put("access_token", tokenManager.getToken());
        return httpAPIService.callForienApi(url, map);*/
        return null;
    }

    public Map<String, Object> getMediaArticles(Integer pageNo, Integer limit, List<String> types, String mediaId) {
        String key = SystemConstant.PREFIX_MEDIA_SOURCE.concat(mediaId);
        JSONObject ids = JSONObject.parseObject(redisService.get(key));
        Map<String, String> map = new HashMap<>();
        map.put("sortField", "pubdate");
        map.put("sortType", "desc");
        map.put("dataType", CommonUtil.listToString(types));
        /*map.put("wechatBiz", "MzIyMDU1Nzc0MA==,MzAxNTQ4OTgyMw==");
        types.stream().forEach(type -> {
            if (ids.containsKey(type) && !StringUtils.isEmpty(ids.getString(type))) {
                switch (type) {
                    case SystemConstant.SOURCE_WECHAT:
                        map.put("wechatBiz", ids.getString(type));
                        break;
                    case SystemConstant.SOURCE_WEIBO:
                        map.put("weiboId", ids.getString(type));
                        break;
                    case SystemConstant.SOURCE_APP:
                        break;
                    default:
                        break;
                }
            }
        });*/
        //return this.fetchApiArticles(pageNo, limit, mediaId, map);
        return null;
    }

    private Map<String, Object> fetchApiArticles(Integer pageNo, Integer limit, String mediaId, Map<String, String> map) {
        JSONArray weekDays = DateParseUtil.lastWeekMondayToSunday();
        map.put("beginTime", weekDays.getString(0));
        map.put("endTime", weekDays.getString(1));
        map.put("access_token", tokenManager.getToken());
        map.put("page", pageNo == null ? "1" : String.valueOf(pageNo));
        map.put("pagesize", limit == null ? "10" : String.valueOf(limit));
        logger.info("------------------------ getMediaArticles, time: {}, map: {} --------------------",
                DateParseUtil.dateTimeToString(new Date()), FastJsonConvertUtil.convertObjectToString(map));
        String url = "/api/query/media";
        Map<String, Object> resMap = httpAPIService.callForienApi(url, map);
        return resMap;
    }

    @Async
    public void asyncInsertEs(String mediaId, Map<String, Object> resMap) {
        JSONObject json = (JSONObject) resMap.get("result");
        if (json.containsKey("rows")) {
            /*logger.info("------------------------ getMediaArticles, time: {}, url: {}, documents: {} --------------------",
                    DateParseUtil.dateTimeToString(new Date()), url, json.getString("rows"));*/
            json.put("mediaId", mediaId);
            List<MediaArticle> mas = MediaArticle.docsParseToList(json);
            if (mas != null) {
                /*for (MediaArticle ma : mas) {
                    logger.info("------------------------ asyncInsertEs, insertToEs, mas id: {} --------------------", ma.getId());
                    esService.createMediaArticle(ma);
                }*/
                logger.info("------------------------ asyncInsertEs, bulk size: {} --------------------", mas.size());
                esService.bulkPutIndex(mas);
            }
        }
    }

    private String getMediaCode(Boolean ignoreAcode, String mediaId) {
        String pscode = null;
        try {
            String path = ApiService.class.getClassLoader().getResource("conf/media-conf.json").getPath();
            JSONArray array = JSONArray.parseArray(JsonUtil.readJsonFile(path));
            JSONObject json = null;
            for (Object o : array) {
                JSONObject jb = (JSONObject) o;
                if (jb.getString("mediaId").equals(mediaId)) {
                    json = jb;
                }
            }
            if (json == null) {
                return "";
            }
            JSONObject mcode = new JSONObject();
            mcode.put("mcode", json.getString("mediaId"));
            if (!ignoreAcode) {
                mcode.put("acode", json.getString("codes"));
            }
            mcode.put("ucode", json.getString("uids"));
            String text = mcode.toString();
            byte[] textByte;
            try {
                textByte = text.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }
            //编码
            Base64.Encoder encoder = Base64.getEncoder();
            String encodedText = encoder.encodeToString(textByte);
            pscode = URLEncoder.encode(encodedText, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pscode;
    }

    public void cacheMediaSource(Map<String, Object> queryMap) {
        try {
            String key = SystemConstant.PREFIX_MEDIA_SOURCE.concat((String) queryMap.get("mediaId"));
            String result = (String) queryMap.get("result");
            JSONObject json = JSONObject.parseObject(result);
            List<MediaSource> mediaSources = FastJsonConvertUtil.convertArrayToList(json.getString("sources"), MediaSource.class);
            List<String> ids_wechat = new ArrayList<>();
            List<String> ids_weibo = new ArrayList<>();
            List<String> ids_app = new ArrayList<>();
            if (!redisService.exists(key) || redisService.getList(key) == null) {
                for (MediaSource ms : mediaSources) {
                    switch (ms.getType()) {
                        case SystemConstant.SOURCE_WECHAT:
                            ids_wechat.add(ms.getId());
                            break;
                        case SystemConstant.SOURCE_WEIBO:
                            ids_weibo.add(ms.getId());
                            break;
                        case SystemConstant.SOURCE_APP:
                            ids_app.add(ms.getId());
                            break;
                        default:
                            break;
                    }
                }
                JSONObject ids = new JSONObject();
                this.setIdsByType(ids_wechat, ids, SystemConstant.SOURCE_WECHAT);
                this.setIdsByType(ids_weibo, ids, SystemConstant.SOURCE_WEIBO);
                this.setIdsByType(ids_app, ids, SystemConstant.SOURCE_APP);
                redisService.set(key, ids.toString(), 24 * 3600);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("===================== cacheMediaSource occure error, mesage: {} =====================", e.getMessage());
        }

    }

    private void setIdsByType(List<String> ids_wechat, JSONObject ids, String jsonKey) {
        if (ids_wechat.size() > 0) {
            ids.put(jsonKey, CommonUtil.listToString(ids_wechat));
        }
    }

    public void fetchAndPutData(String mediaId, String codes, String names, String types) {
        Map<String, String> map = new HashMap<>();
        map.put("sortField", "pubdate");
        map.put("sortType", "asc");
        map.put("dataType", types);
        // 单一信源查询(信源--统计页面)
        if (StringUtils.isEmpty(types)) {
            logger.error("========================== fetchAndPutData types are empty  =====================");
            return ;
        }
        switch (types) {
            case SystemConstant.SOURCE_WECHAT:
                map.put("wechatBiz", types);
                break;
            case SystemConstant.SOURCE_WEIBO:
                map.put("weiboId", types);
                break;
            default:
                map.put("siteId", types);
                break;
        }
        Integer pageNo = 1;
        Integer limit = 1000;
        Map<String, Object> resMap = this.fetchApiArticles(pageNo, limit, mediaId, map);
        if (resMap.get("result") == null) {
            return;
        }
        Integer pageCount = JSONObject.parseObject(resMap.get("result").toString()).getInteger("pageCount") + 1;
        this.asyncInsertEs(mediaId, resMap);
        for (int i = 2; i < pageCount; i++) {
            resMap = this.fetchApiArticles(i, limit, mediaId, map);
            if (resMap.get("result") == null) {
                break;
            }
            this.asyncInsertEs(mediaId, resMap);
        }
    }

    public Map<String, Object> queryMediaStats(String mediaId, String codes, String names, String types) throws IOException {
        String key = SystemConstant.PREFIX_MEDIA_SOURCE.concat(mediaId);
        if (!redisService.exists(key) || redisService.get(key) == null) {
            codes = URLDecoder.decode(codes, "UTF-8");
            this.fetchAndPutData(mediaId, codes, names, types);
            this.cacheMediaConf(mediaId, codes, types, key);
        }
        return esService.querayMediaStats(mediaId, DateParseUtil.queryTodayWeekOfYear(new Date()));
    }

    private void cacheMediaConf(String mediaId, String codes, String types, String key) {
        redisService.set(key, codes, -1);
        String path = ApiService.class.getClassLoader().getResource("conf/media-conf.json").getPath();
        JSONArray array = new JSONArray();
        String jsonConf = JsonUtil.readJsonFile(path);
        if (!StringUtils.isEmpty(jsonConf)) {
            array = JSONArray.parseArray(jsonConf);
        }
        JSONArray finalArray = new JSONArray();
        finalArray.addAll(array);
        array.stream().forEach(a -> {
            JSONObject json = (JSONObject) a;
            if (json.getString("mediaId").equals(mediaId)) {
                finalArray.remove(json);
            }
        });
        JSONObject json = new JSONObject();
        json.put("mediaId", mediaId);
        json.put("codes", codes);
        json.put("types", types);
        finalArray.add(json);
        JsonUtil.writeJson(finalArray.toString(), path);
    }
}
