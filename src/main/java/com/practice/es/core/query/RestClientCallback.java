package com.practice.es.core.query;

import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;

public interface RestClientCallback<T> {

    T doInRestClient(RestHighLevelClient client) throws IOException;
}
