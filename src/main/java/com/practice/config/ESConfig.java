package com.practice.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class ESConfig {
//    @Value("${spring.data.elasticsearch.cluster-name}")
    //private String esIP;


    /*@Bean
    public RestHighLevelClient client() {
        RestHighLevelClient client = new RestHighLevelClient(
                //这里如果要用client去访问其他节点，就添加进去
                RestClient.builder(new HttpHost(esIP, 9200, "http")));
        return client;
    }*/

}