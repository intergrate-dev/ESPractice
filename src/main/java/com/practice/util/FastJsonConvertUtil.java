package com.practice.util;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class FastJsonConvertUtil<T> {

    public static String convertObjectToJSON(Object object) {
        return JSON.toJSONString(object);

    }

    public static Object convertJSONToObject(String message, Class<Object> clazz) {
        JSONObject json = JSONObject.parseObject(message);
        return json.toJavaObject(clazz);
    }

    public static Object convertJSONToObject(JSONObject json) {
        return JSONObject.toJavaObject(json, Object.class);
    }

    public static JSONObject toJsonObject(Object javaBean) {
        return JSONObject.parseObject(JSONObject.toJSON(javaBean).toString());
    }

}
