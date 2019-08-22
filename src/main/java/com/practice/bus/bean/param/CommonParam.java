package com.practice.bus.bean.param;

import javax.validation.constraints.Pattern;
import java.io.Serializable;

public class CommonParam implements Serializable {

    @Pattern(regexp="^[0-9]+$", message="参数media格式不正确")
    private String mediaId;
    private String name;

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
