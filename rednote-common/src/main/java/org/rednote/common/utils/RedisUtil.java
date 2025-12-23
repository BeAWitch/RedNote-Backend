package org.rednote.common.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类
 */
@RequiredArgsConstructor
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    public void expire(String key, long timeoutSeconds, TimeUnit timeUnit) {
        redisTemplate.expire(key, timeoutSeconds, timeUnit);
    }

    // ===================
    // 普通 Key-Value 操作
    // ===================

    public void set(String key, Object value, long timeoutSeconds, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, timeoutSeconds, timeUnit);
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // ===================
    // Hash 操作
    // ===================

    /**
     * 存单个字段
     */
    public void hSet(String key, String hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    /**
     * 获取单个字段
     */
    public Object hGet(String key, String hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }

    /**
     * 存整个 Map
     */
    public void hSetAll(String key, Map<String, Object> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    /**
     * 获取整个 Map
     */
    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 删除 Hash 中的字段
     */
    public void hDelete(String key, String... hashKeys) {
        redisTemplate.opsForHash().delete(key, (Object[]) hashKeys);
    }

    /**
     * 判断 Hash 中字段是否存在
     */
    public boolean hExists(String key, String hashKey) {
        return Boolean.TRUE.equals(redisTemplate.opsForHash().hasKey(key, hashKey));
    }

    /**
     * 给 Hash 中的数值字段自增
     */
    public long hIncrement(String key, String hashKey, long delta) {
        return redisTemplate.opsForHash().increment(key, hashKey, delta);
    }

    /**
     * 给 Hash 中的数值字段自增（浮点数）
     */
    public double hIncrement(String key, String hashKey, double delta) {
        return redisTemplate.opsForHash().increment(key, hashKey, delta);
    }
}

