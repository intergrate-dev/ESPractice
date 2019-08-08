package com.practice.es.core;


public class RestClientAccessor {

    private ElasticsearchClientFactory restClientFactory;

    public RestClientAccessor(){
    }

    public RestClientAccessor(ElasticsearchClientFactory restClientFactory){
        this.restClientFactory = restClientFactory;
    }

    public void setConnectionFactory(ElasticsearchClientFactory restClientFactory) {
        this.restClientFactory = restClientFactory;
    }

    public ElasticsearchClientFactory getRestClientFactory() {
        if (restClientFactory == null) {
            throw new IllegalStateException("RestHighLevelClientFactory is required");
        }
        return restClientFactory;
    }
}
