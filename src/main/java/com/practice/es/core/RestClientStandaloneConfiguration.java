package com.practice.es.core;

import org.apache.http.HttpHost;

public class RestClientStandaloneConfiguration extends RestClientConfiguration {

    private String host;
    private Integer port;

    public RestClientStandaloneConfiguration(String host, Integer port) {
        super();
    }

    @Override
    HttpHost[] getHttpHosts() {
        return new HttpHost[0];
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
