package com.practice.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.practice.bus.service.ApiService;
import org.apache.commons.lang3.StringUtils;

import java.io.*;

public class JsonUtil {
    public static String readJsonFile(String fileName) {
        String jsonStr = "";
        try {
            File jsonFile = new File(fileName);
            //FileReader fileReader = new FileReader(jsonFile);

            Reader reader = new InputStreamReader(new FileInputStream(jsonFile), "utf-8");
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            //fileReader.close();
            reader.close();
            jsonStr = sb.toString();

            return jsonStr;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String readFromResStream(String resPath) {
        InputStream inputStream = JsonUtil.class.getClassLoader().getResourceAsStream(resPath);
        String jsonStr = "";
        try {
            //File jsonFile = new File(fileName);
            //FileReader fileReader = new FileReader(jsonFile);

            //Reader reader = new InputStreamReader(new FileInputStream(jsonFile), "utf-8");
            Reader reader = new InputStreamReader(inputStream, "utf-8");
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            //fileReader.close();
            reader.close();
            jsonStr = sb.toString();

            return jsonStr;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void writeJson(String content, String fileName) {
        FileOutputStream o = null;
        try {
            File file = new File(fileName);
            o = new FileOutputStream(file);
            o.write(content.getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                o.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        //String path = ApiService.class.getClassLoader().getResource("conf/media-conf.json").getPath();
        //String jsonConf = JsonUtil.readJsonFile(path);
        JSONArray array = new JSONArray();
        String jsonConf = JsonUtil.readFromResStream("conf/media-conf.json");
        if (!StringUtils.isEmpty(jsonConf)) {
            array = JSONArray.parseArray(jsonConf);
        }
        String mediaId = "-100";
        JSONArray finalArray = new JSONArray();
        finalArray.addAll(array);
        array.stream().forEach(a -> {
            JSONObject json = (JSONObject) a;
            if (json.getString("mediaId").equals(mediaId)) {
                finalArray.remove(json);
            }
        });
        JSONObject json = new JSONObject();
        String codes = "56546hgfhfgh,rtgregr";
        String types = "rtrtr,8888,hhhhh";
        json.put("mediaId", mediaId);
        json.put("codes", codes);
        json.put("types", types);
        finalArray.add(json);
        //JsonUtil.writeJson(finalArray.toString(), path);
    }
}
