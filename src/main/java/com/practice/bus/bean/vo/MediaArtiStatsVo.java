package com.practice.bus.bean.vo;

import java.io.Serializable;
import java.util.List;

/**
 * @author yuan-pc
 */
public class MediaArtiStatsVo implements Serializable {

    public static final String TOTAL_VISIT = "visitTotal";
    public static final String TOTAL_LIKE = "likeTotal";

    private String id;
    /**
     * 公众号名称
     */
    private String sourceName;
    /**
     * 浏览量
     */
    private List<String> scan;
    /**
     * 点赞数
     */
    private List<String> like;
    /**
     * 发稿数
     */
    private List<String> publish;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public List<String> getScan() {
        return scan;
    }

    public void setScan(List<String> scan) {
        this.scan = scan;
    }

    public List<String> getLike() {
        return like;
    }

    public void setLike(List<String> like) {
        this.like = like;
    }

    public List<String> getPublish() {
        return publish;
    }

    public void setPublish(List<String> publish) {
        this.publish = publish;
    }
}
