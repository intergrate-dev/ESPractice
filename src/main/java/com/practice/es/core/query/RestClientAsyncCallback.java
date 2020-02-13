package com.practice.es.core.query;

import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;

public interface RestClientAsyncCallback {

    void doInRestAsyncClient(RestHighLevelClient client) throws IOException;
}
