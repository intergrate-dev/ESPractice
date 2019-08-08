package com.practice.es.service;

import lombok.Data;

import java.io.Serializable;


@Data
public class EsObject<T> implements Serializable {
    private String index;
    private String type;
    private String id;
    private String version;
    private String seqNo;
    private String primaryTerm;
    private Boolean found;
    private T source;
}
