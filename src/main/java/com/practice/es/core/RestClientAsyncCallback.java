package com.practice.es.core;

import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;

public interface RestClientAsyncCallback {

    void doInRestAsyncClient(RestHighLevelClient client) throws IOException;
}
