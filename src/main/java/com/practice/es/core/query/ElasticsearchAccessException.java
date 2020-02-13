package com.practice.es.core.query;

public class ElasticsearchAccessException extends RuntimeException {

    public ElasticsearchAccessException(String msg) {
        super(msg);
    }

    public ElasticsearchAccessException(Throwable cause) {
        super(cause);
    }

    public ElasticsearchAccessException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
