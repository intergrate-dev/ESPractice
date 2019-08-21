package com.practice.bus.bean;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.practice.util.DateParseUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class MediaArticle implements Serializable {
    private String id;
    /**
     * 公众号、微博名称
     */
    private String name;
    private String code;
    /**
     * 信源（所属站点/微信公众号/微博博主)的ID
     */
    private int sourceId;
    private int mediaId;
    /**
     * 公众号、微博编码
     */
    private String dataType;
    private String title;
    private int visitCount;
    private int likeCount;
    /*@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date pubdate;*/
    private String pubdate;
    private String location;
    private Integer weekOfYear;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getSourceId() {
        return sourceId;
    }

    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }

    public int getMediaId() {
        return mediaId;
    }

    public void setMediaId(int mediaId) {
        this.mediaId = mediaId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getVisitCount() {
        return visitCount;
    }

    public void setVisitCount(int visitCount) {
        this.visitCount = visitCount;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    /*public Date getPubdate() {
        return pubdate;
    }

    public void setPubdate(Date pubdate) {
        this.pubdate = pubdate;
    }*/

    public String getPubdate() {
        return pubdate;
    }

    public void setPubdate(String pubdate) {
        this.pubdate = pubdate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getWeekOfYear() {
        return weekOfYear;
    }

    public void setWeekOfYear(Integer weekOfYear) {
        this.weekOfYear = weekOfYear;
    }

    public static List<MediaArticle> docsParseToList(JSONObject jsonObject) {
        if (!jsonObject.containsKey("rows") || StringUtils.isEmpty(jsonObject.getString("rows"))) {
            return null;
        }

        JSONArray array = JSONArray.parseArray(jsonObject.getString("rows"));
        List<MediaArticle> list = new ArrayList<>();
        array.stream().forEach(a -> {
            JSONObject json = (JSONObject) a;
            MediaArticle ma = new MediaArticle();
            ma.setId(json.getString("id"));
            ma.setDataType(json.getString("dataType"));
            ma.setMediaId(Integer.parseInt(jsonObject.getString("mediaId")));
            if (json.containsKey("author")) {
                JSONObject author = JSONObject.parseObject(json.getString("author"));
                ma.setCode(author.getString("code"));
                ma.setName(author.getString("nickName"));
            }
            ma.setTitle(json.getString("title"));
            //ma.setPubdate(DateParseUtil.stringToDateTime(json.getString("pubdate")));
            ma.setPubdate(json.getString("pubdate"));
            ma.setVisitCount(Integer.parseInt(json.getString("visitCount")));
            ma.setLikeCount(Integer.parseInt(json.getString("likeCount")));
            ma.setLocation(json.getString("location"));
            ma.setWeekOfYear(DateParseUtil.queryTodayWeekOfYear(new Date()));
            list.add(ma);
        });
        return list;
    }

    public static String genRequestId(MediaArticle entity) {
        //return entity.getMediaId() + "-" + entity.getCode() + "-" + entity.getWeekOfYear();
        return entity.getMediaId() + "|" + entity.getCode() + "|" + entity.getWeekOfYear() + "|" + entity.getId();
    }
}
