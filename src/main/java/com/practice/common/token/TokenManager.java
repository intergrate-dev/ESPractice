package com.practice.common.token;

import com.alibaba.fastjson.JSONObject;
import com.practice.common.http.HttpAPIService;
import com.practice.common.redis.RedisService;
import com.practice.config.BigScreenConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TokenManager {

    private static Logger logger = LoggerFactory.getLogger(TokenManager.class);

    @Autowired
    HttpAPIService httpAPIService;

    @Autowired
    RedisService redisService;

    @Autowired
    BigScreenConfig bigScreenConfig;

    public String getToken() {
        String access_token = null;
        String key = "sitemonitor:".concat("access_token");
        if (redisService.exists(key)) {
            access_token = redisService.get(key);
            if (!StringUtils.isEmpty(access_token)) {
                logger.info("--------------- getToken, access_token: {} ------------", access_token);
                return access_token;
            }
        }
        String url = bigScreenConfig.getRooturl() + "/api/token";
        Map<String, String> map = new HashMap<String, String>();
        map.put("appid", bigScreenConfig.getAPPID());
        map.put("secret", bigScreenConfig.getAPPSECRET());
        String tokenRes = httpAPIService.doGet(url, map);
        if (tokenRes != null) {
            JSONObject tokjson = JSONObject.parseObject(tokenRes);
            int code = tokjson.getIntValue("errcode");
            if (code == 0) {
                access_token = tokjson.getString("access_token");
                redisService.set(key, access_token, Integer.parseInt(tokjson.getString("expires_in")) - 1000);
            }
        }
        logger.info("--------------- getToken, access_token: {} ------------", access_token);
        return access_token;
    }

    public TokenEntity getActToken() {
        return null;
    }
}
