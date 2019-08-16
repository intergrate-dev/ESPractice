package com.practice.bus.service;

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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
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
        // TODO 媒体文件配置表整理
        String url = "/api/source";
        Map<String, String> map = new HashMap<>();
        map.put("type", CommonUtil.listToString(types));
        map.put("mediaId", mediaId);
        map.put("access_token", tokenManager.getToken());
        return httpAPIService.callForienApi(url, map);
    }

    public Map<String, Object> getMediaArticles(Integer pageNo, Integer limit, List<String> types, String mediaId) {
        String key = SystemConstant.PREFIX_MEDIA_SOURCE.concat(mediaId);
        JSONObject ids = JSONObject.parseObject(redisService.get(key));
        Map<String, String> map = new HashMap<>();
        map.put("sortField", "pubdate");
        map.put("sortType", "desc");
        map.put("dataType", CommonUtil.listToString(types));
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
        });
        return this.extract(pageNo, limit, mediaId, map);
    }

    private Map<String, Object> extract(Integer pageNo, Integer limit, String mediaId, Map<String, String> map) {
        if (!StringUtils.isEmpty(mediaId)) {
            map.put("mediaIds", mediaId);
        }
        JSONArray weekDays = CommonUtil.lastWeekMondayToSunday();
        map.put("beginTime", weekDays.getString(0));
        map.put("endTime", weekDays.getString(1));
        map.put("access_token", tokenManager.getToken());
        map.put("page", pageNo == null ? "1" : String.valueOf(pageNo));
        map.put("pagesize", limit == null ? "10" : String.valueOf(limit));
        logger.info("------------------------ getMediaArticles, map: {} --------------------", map);
        String url = "/api/query/media";
        Map<String, Object> resMap = httpAPIService.callForienApi(url, map);
        // TODO asyn
        JSONObject json = (JSONObject) resMap.get("result");
        if (json.containsKey("documents")) {
            List<MediaArticle> mas = MediaArticle.docsParseToList(json.getString("documents"));
            if (mas != null) {
                // TODO if need, modify batch insert, via cache
                for (MediaArticle ma : mas) {
                    esService.createMediaArticle(ma);
                }
            }
        }
        return resMap;
    }

    private String getMediaCode(Boolean ignoreAcode, String mediaId) {
        String pscode = null;
        try {
            String path = ApiService.class.getClassLoader().getResource("conf/media-conf.json").getPath();
            JSONArray array = JSONArray.parseArray(JsonUtil.readJsonFile(path));
            JSONObject json = null;
            for (Object o : array) {
                JSONObject jb = (JSONObject) o;
                if (jb.getString("ids").equals(mediaId)) {
                    json = jb;
                }
            }
            if (json == null) {
                return "";
            }
            JSONObject mcode = new JSONObject();
            mcode.put("mcode", json.getString("ids"));
            if (!ignoreAcode) {
                mcode.put("acode", json.getString("aids"));
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

    public void sourceInit(String mediaId, String ids, String names, List<String> types) {
        Map<String, String> map = new HashMap<>();
        map.put("sortField", "pubdate");
        map.put("sortType", "desc");
        map.put("dataType", CommonUtil.listToString(types));
        map.put("wechatBiz", ids);
        Integer pageNo = 1;
        Integer limit = 5000;
        this.extract(pageNo, limit, mediaId, map);
    }

    public Map<String, Object> queryMediaStats(String mediaId) {
        return esService.querayMediaStats(mediaId, DateParseUtil.queryTodayWeekOfYear(new Date()));
    }
}
