package com.practice.bus.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.practice.bus.bean.MediaArticle;
import com.practice.bus.bean.param.MediaStatsParam;
import com.practice.common.Constant;
import com.practice.common.http.HttpAPIService;
import com.practice.common.redis.RedisService;
import com.practice.common.token.TokenManager;
import com.practice.config.BigScreenConfig;
import com.practice.es.service.ESService;
import com.practice.util.CommonUtil;
import com.practice.util.DateParseUtil;
import com.practice.util.FastJsonConvertUtil;
import com.practice.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;
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
        String key = Constant.PREFIX_MEDIA_SOURCE.concat(mediaId);
        JSONObject ids = JSONObject.parseObject(redisService.get(key));
        Map<String, String> map = new HashMap<>();
        map.put("sortField", "pubdate");
        map.put("sortType", "desc");
        map.put("dataType", CommonUtil.listToString(types));
        /*map.put("wechatBiz", "MzIyMDU1Nzc0MA==,MzAxNTQ4OTgyMw==");
        types.stream().forEach(type -> {
            if (ids.containsKey(type) && !StringUtils.isEmpty(ids.getString(type))) {
                switch (type) {
                    case Constant.SOURCE_WECHAT:
                        map.put("wechatBiz", ids.getString(type));
                        break;
                    case Constant.SOURCE_WEIBO:
                        map.put("weiboId", ids.getString(type));
                        break;
                    case Constant.SOURCE_APP:
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
            JSONArray array = JSONArray.parseArray(JsonUtil.readFromResStream("conf/media-conf.json"));
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
            String key = Constant.PREFIX_MEDIA_SOURCE.concat((String) queryMap.get("mediaId"));
            String result = (String) queryMap.get("result");
            JSONObject json = JSONObject.parseObject(result);
            List<String> ids_wechat = new ArrayList<>();
            List<String> ids_weibo = new ArrayList<>();
            List<String> ids_app = new ArrayList<>();
            if (!redisService.exists(key) || redisService.getList(key) == null) {
                /*for (MediaSource ms : mediaSources) {
                    switch (ms.getType()) {
                        case Constant.SOURCE_WECHAT:
                            ids_wechat.add(ms.getId());
                            break;
                        case Constant.SOURCE_WEIBO:
                            ids_weibo.add(ms.getId());
                            break;
                        case Constant.SOURCE_APP:
                            ids_app.add(ms.getId());
                            break;
                        default:
                            break;
                    }
                }*/
                JSONObject ids = new JSONObject();
                this.setIdsByType(ids_wechat, ids, MediaArticle.SOURCE_WECHAT);
                this.setIdsByType(ids_weibo, ids, MediaArticle.SOURCE_WEIBO);
                this.setIdsByType(ids_app, ids, MediaArticle.SOURCE_APP);
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

    public void fetchAndPutData(MediaStatsParam param) {
        Map<String, String> map = new HashMap<>();
        String types = param.getTypes();
        String codes = param.getCodes();
        String mediaId = param.getMediaId();
        map.put("sortField", "pubdate");
        map.put("sortType", "asc");
        map.put("dataType", types);
        // 单一信源查询(信源--统计页面)
        if (StringUtils.isEmpty(types)) {
            logger.error("========================== fetchAndPutData types are empty  =====================");
            return ;
        }
        switch (types) {
            case MediaArticle.SOURCE_WECHAT:
                map.put("wechatBiz", codes);
                break;
            case MediaArticle.SOURCE_WEIBO:
                map.put("weiboId", codes);
                break;
            default:
                map.put("siteId", codes);
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

    public Map<String, Object> queryMediaStats(MediaStatsParam param) throws IOException {
        String key = Constant.PREFIX_MEDIA_SOURCE.concat(param.getMediaId());
        if (!redisService.exists(key) || redisService.get(key) == null) {
            param.setCodes(URLDecoder.decode(param.getCodes(), "UTF-8"));
            this.fetchAndPutData(param);
            this.cacheMediaConf(param, key);
        }
        return esService.querayMediaStats(param.getMediaId(), DateParseUtil.queryTodayWeekOfYear(new Date()));
    }

    private void cacheMediaConf(MediaStatsParam param, String key) {
        redisService.set(key, param.getCodes(), -1);
        JSONArray array = new JSONArray();
        JSONArray finalArray = new JSONArray();
        String keyConf = Constant.KEY_MEDIA_SOURCE_CONF;
        if (redisService.exists(keyConf)) {
            array = JSONArray.parseArray(redisService.get(keyConf));
            finalArray.addAll(array);
        }
        array.stream().forEach(a -> {
            JSONObject json = (JSONObject) a;
            if (json.getString("mediaId").equals(param.getMediaId())) {
                finalArray.remove(json);
            }
        });
        finalArray.add(FastJsonConvertUtil.convertObjectToJSON(param));
        redisService.set(keyConf, finalArray.toString(), -1);
    }
}
