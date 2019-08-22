package com.practice.bus.bean.param;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

public class MediaStatsParam extends CommonParam {

    @NotBlank
    private String codes;
    @NotBlank
    private String types;

    public String getCodes() {
        return codes;
    }

    public void setCodes(String codes) {
        this.codes = codes;
    }

    public String getTypes() {
        return types;
    }

    public void setTypes(String types) {
        this.types = types;
    }
}
