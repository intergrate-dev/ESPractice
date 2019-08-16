package com.practice.bus.bean;

public class MediaSource {

    private String id;
    private String code;
    private String type;
    private String name;

    public MediaSource() {
    }

    public MediaSource(String id, String code, String type, String name) {
        this.id = id;
        this.code = code;
        this.type = type;
        this.name = name;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
