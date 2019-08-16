package com.practice.common.redis;

import com.alibaba.fastjson.JSON;
import com.practice.util.SerializeUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;

/**
 * redis服务
 */
@Service
public class RedisService {
    private static Logger logger = LoggerFactory.getLogger(RedisService.class);

    @Autowired
    JedisPool jedisPool;

    public Boolean set(KeyPrefix prefix, String key, String value) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            if (StringUtils.isEmpty(value)) {
                return false;
            }
            String realKey = prefix.getPrefix() + key;
            int seconds = prefix.expireSeconds();
            if (seconds <= 0) {
                jedis.set(realKey, value);
            } else {
                jedis.setex(realKey, seconds, value);
            }
            return true;
        } finally {
            returnToPool(jedis);
        }
    }

    public String get(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix() + key;
            return jedis.get(realKey);
        } finally {
            returnToPool(jedis);
        }

    }

    public String get(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            //return (String) SerializeUtil.unserialize(jedis.get(key).getBytes());
            return jedis.get(key);
        } finally {
            returnToPool(jedis);
        }

    }

    /**
     * 从redis连接池获取redis实例
     */
    public <T> T get(KeyPrefix prefix, String key, Class<T> clazz) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix() + key;
            String str = jedis.get(realKey);
            T t = (T) SerializeUtil.unserialize(str.getBytes());
            return t;
        } finally {
            returnToPool(jedis);
        }

    }

    /**
     * 存储对象
     */
    public <T> Boolean set(KeyPrefix prefix, String key, T value) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String str = SerializeUtil.serialize(value).toString();
            if (str == null || str.length() <= 0) {
                return false;
            }
            String realKey = prefix.getPrefix() + key;
            int seconds = prefix.expireSeconds();
            if (seconds <= 0) {
                jedis.set(realKey, str);
            } else {
                jedis.setex(realKey, seconds, str);
            }

            return true;
        } finally {
            returnToPool(jedis);
        }

    }

    public <T> Boolean set(String key, T value, Integer expire) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            //String str = SerializeUtil.serialize(value).toString();
            String str = (String) value;
            if (str == null || str.length() <= 0) {
                return false;
            }
            if (expire <= 0) {
                jedis.set(key, str);
            } else {
                jedis.setex(key, expire, str);
            }
            return true;
        } finally {
            returnToPool(jedis);
        }
    }

    /**
     * 删除
     */
    public boolean delete(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix() + key;
            long ret = jedis.del(realKey);
            return ret > 0;
        } finally {
            returnToPool(jedis);
        }
    }

    /**
     * 判断key是否存在
     */
    public <T> boolean exists(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix() + key;
            return jedis.exists(realKey);
        } finally {
            returnToPool(jedis);
        }
    }

    public <T> boolean exists(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.exists(key);
        } finally {
            returnToPool(jedis);
        }
    }

    /**
     * 增加值
     * Redis Incr 命令将 key 中储存的数字值增一。    如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 INCR 操作
     */
    public <T> Long incr(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix() + key;
            return jedis.incr(realKey);
        } finally {
            returnToPool(jedis);
        }
    }

    /**
     * 减少值
     */
    public <T> Long decr(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix() + key;
            return jedis.decr(realKey);
        } finally {
            returnToPool(jedis);
        }
    }

    public static <T> String beanToString(T value) {
        if (value == null) {
            return null;
        }
        Class<?> clazz = value.getClass();
        if (clazz == int.class || clazz == Integer.class) {
            return String.valueOf(value);
        } else if (clazz == long.class || clazz == Long.class) {
            return String.valueOf(value);
        } else if (clazz == String.class) {
            return (String) value;
        } else {
            return JSON.toJSONString(value);
        }
    }

    public static <T> T stringToBean(String str, Class<T> clazz) {
        if (str == null || str.length() <= 0 || clazz == null) {
            return null;
        }
        if (clazz == int.class || clazz == Integer.class) {
            return (T) Integer.valueOf(str);
        } else if (clazz == long.class || clazz == Long.class) {
            return (T) Long.valueOf(str);
        } else if (clazz == String.class) {
            return (T) str;
        } else {
            return JSON.toJavaObject(JSON.parseObject(str), clazz);
        }
    }

    private void returnToPool(Jedis jedis) {
        if (jedis != null) {
            jedis.close();//不是关闭，只是返回连接池
        }
    }

    public void setList(String key, List<?> list, Integer expire) {
        Jedis jedis = jedisPool.getResource();
        try {
            if (list != null && list.size() > 0) {
                jedis.setex(key.getBytes(), expire, SerializeUtil.serializeList(list));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }
    }

    public void setList(String key, List<?> list) {
        Jedis jedis = jedisPool.getResource();
        try {
            if (list != null && list.size() > 0) {
                jedis.set(key.getBytes(), SerializeUtil.serializeList(list));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }
    }

    public List<?> getList(String key) {
        Jedis jedis = null;
        byte[] data = null;
        try {
            jedis = jedisPool.getResource();
            if (jedis != null && jedis.exists(key)) {
                data = jedis.get(key.getBytes());
                if (data != null) {
                    return SerializeUtil.unserializeList(data);
                }
            }
        } catch (Exception e) {
            logger.error("Set key error : " + e);
        } finally {
            jedis.close();
        }
        return null;
    }

}
