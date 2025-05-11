package com.liucc.web.manager;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 缓存中间件 操作
 *
 * @author liucc
 * @from <a href="https://github.com/dashboard">tiga</a>
 */
@Component
public class CacheManager {

    @Resource
    private RedisTemplate redisTemplate;

    Cache<String, Object> cache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    /**
     * 查找缓存，如果不存在，则返回 null
     *
     * @param key
     * @return
     */
    public Object get(String key){
        Object value = cache.getIfPresent(key);
        if(BeanUtil.isNotEmpty(value)){
            return value;
        }
        // 本地缓存不存在，redis 查找
        value = redisTemplate.opsForValue().get(key);
        if(BeanUtil.isNotEmpty(value)){
            cache.put(key, value);
            return value;
        }
        return null;
    }


    /**
     * 更新缓存
     *
     * @param key
     * @param value
     */
    public void put(String key, Object value){
        // 添加或者更新一个缓存元素
        cache.put(key, value);
    }

    /**
     * 移除缓存
     *
     * @param key
     */
    public void delete(String key) {
        // 移除一个缓存元素
        cache.invalidate(key);
    }

}
