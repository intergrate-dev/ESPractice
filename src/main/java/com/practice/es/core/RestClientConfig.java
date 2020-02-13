package com.practice.es.core;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestClientConfig {
    @Value("${spring.data.elasticsearch.cluster-name}")
    private String esIP;

    /*public HttpHost[] getHttpHosts() {
        HttpHost[] hosts = {new HttpHost(esIP, 9200)};
        return hosts;
    }*/

    /*@Bean
    public RestHighLevelClient client() {
        RestHighLevelClient client = new RestHighLevelClient(
                //这里如果要用client去访问其他节点，就添加进去
                RestClient.builder(new HttpHost(esIP, 9200, "http")));
        return client;
    }*/

    /**
     * 配置RestClient连接池，基于commons-pool2
     */
    @Bean
    public RestClientPoolConfig poolConfig(){
        RestClientPoolConfig poolConfig = new RestClientPoolConfig();
        poolConfig.setMinIdle(5);
        poolConfig.setMaxTotal(20);
        poolConfig.setMaxWaitMillis(2000);
        //other...
        return poolConfig;
    }

    /**
     * 装配RestClient配置类(单机)
     */
    @Bean
    public RestClientConfiguration restClientConfiguration(){
        RestClientStandaloneConfiguration configuration = new RestClientStandaloneConfiguration(esIP,9200);
        configuration.setConnectTimeout(1000);
        configuration.setConnectionRequestTimeout(500);
        configuration.setSocketTimeout(20000);
        configuration.setMaxConnTotal(100);
        configuration.setMaxConnPerRoute(100);
        return configuration;
    }

    /**
     * 装配ElasticsearchClientFactory
     */
    @Bean
    public ElasticsearchClientFactory elasticsearchClientFactory(RestClientConfiguration configuration, RestClientPoolConfig poolConfig){
        configuration = new RestClientStandaloneConfiguration(esIP,9200);
        ElasticsearchClientFactory factory = new ElasticsearchClientFactory(configuration, poolConfig);
        //如果需要，可以设置默认的请求头信息
        //Map<String, String> headers = new HashMap<>();
        //headers.put("key", "value");
        //factory.setDefaultHeaders(headers);
        return factory;
    }

    /**
     * 装配RestClientTemplate
     */
    @Bean
    public RestClientTemplate restClientTemplate(ElasticsearchClientFactory factory){
        return new RestClientTemplate(factory);
    }
}
