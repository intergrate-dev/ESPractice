package com.practice.es.core;


/**
 * @author wangl
 * @date 2019-04-30
 */
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
