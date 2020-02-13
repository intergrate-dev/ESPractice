package com.practice.common.redis;


/**
 * @author yuan-pc
 */
//@Component
public class AppPrefix implements KeyPrefix{
    private int expireSeconds;
    private String prefix;

    /*@Autowired
    AppConfig appConfig;*/

    public static AppPrefix appPrefix = null;

    public AppPrefix() {
    }

    public synchronized static AppPrefix getInstance() {
        if (appPrefix == null) {
            appPrefix = new AppPrefix();
        }
        return appPrefix;
    }

    public AppPrefix(int expireSeconds, String prefix) {
        this.expireSeconds = expireSeconds;
        this.prefix = prefix;
    }

    @Override
    public int expireSeconds() {
        return this.expireSeconds;
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    public void setExpireSeconds(int expireSeconds) {
        this.expireSeconds = expireSeconds;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
