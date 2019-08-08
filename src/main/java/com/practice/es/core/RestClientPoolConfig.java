package com.practice.es.core;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class RestClientPoolConfig extends GenericObjectPoolConfig {

    public RestClientPoolConfig() {
        setTestWhileIdle(true);
        setMinEvictableIdleTimeMillis(60000);
        setTimeBetweenEvictionRunsMillis(30000);
        setNumTestsPerEvictionRun(-1);
    }
}
