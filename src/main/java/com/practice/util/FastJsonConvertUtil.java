package com.practice.util;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.practice.bus.bean.MediaSource;
import com.practice.bus.controller.DocController;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class FastJsonConvertUtil<T> {
    private static Logger logger = LoggerFactory.getLogger(FastJsonConvertUtil.class);

    public static JSONObject convertObjectToJSON(Object object) {
        return JSONObject.parseObject(JSON.toJSONString(object));

    }

    public static String convertObjectToString(Object object) {
        return JSON.toJSONString(object);

    }

    public static <T> T convertJSONToObject(String message, Class<T> clazz) {
        return JSONObject.parseObject(message).toJavaObject(clazz);
    }

    public static <T> List<T> convertArrayToList(String jsonArrayStr, Class<T> clazz) {
        return JSONArray.parseArray(jsonArrayStr).toJavaList(clazz);
    }


    public static Object convertJSONToObject(JSONObject json) {
        return JSONObject.toJavaObject(json, Object.class);
    }

    public static JSONObject toJsonObject(Object javaBean) {
        return JSONObject.parseObject(JSONObject.toJSON(javaBean).toString());
    }

    public static Map<String, Object> json2Map(String jsonStr) {
        Map<String, Object> map = new HashMap<>();
        if (!StringUtils.isEmpty(jsonStr)) {
            JSONObject json = JSONObject.parseObject(jsonStr);
            for (Object k : json.keySet()) {
                Object v = json.get(k);
                if (v instanceof JSONArray) {
                    List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
                    Iterator<Object> iterator = ((JSONArray) v).iterator();
                    while (iterator.hasNext()) {
                        JSONObject json2 = (JSONObject) iterator.next();
                        list.add(json2Map(json2.toString()));
                    }
                    map.put(k.toString(), list);
                } else {
                    map.put(k.toString(), v);
                }
            }
            return map;
        } else {
            return null;
        }
    }

    public static JSONObject map2Json(Map<String, Object> source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        return JSONObject.parseObject(JSONObject.toJSONString(source));
    }
    public static void main(String[] args) {
        /*String path = FastJsonConvertUtil.class.getClassLoader().getResource("mapping-siteMonitor.json").getPath();
        String s = JsonUtil.readJsonFile(path);
        JSONObject json = JSON.parseObject(s);
        Map<String, Object> resMap = json2Map(json.toString());
        logger.info("resMap: {}", resMap);*/

        /*Double ceil = Math.ceil(3 * 1.0 / 10);
        long round = Math.round(ceil);
        logger.info("ceil: {}", round);*/

        /*Map<String, Object> map = new HashMap<>();
        map.put("id", 45454);
        map.put("name", "etirjtiert");
        List<MediaSource> list = new ArrayList<>();
        list.add(new MediaSource("1","code1","type1", "name1"));
        list.add(new MediaSource("2","code2","type2", "name2"));
        map.put("list", list);
        JSONObject json = map2Json(map);
        logger.info("------------------------ main, jsonStr: {} --------------------", json.toString());*/
        logger.info("------------------------ main, week of year: {} --------------------", DateParseUtil.queryTodayWeekOfYear(new Date()));

    }
}
