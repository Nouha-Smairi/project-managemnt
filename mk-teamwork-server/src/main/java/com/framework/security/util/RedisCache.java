package com.framework.security.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @version V1.0
 * @program: teamwork
 * @package: com.framework.security.util
 * @description: redis operation tool class
 * @create: 2020-06-25 10:42
 **/
@Component
public class RedisCache {

    @Autowired
    public RedisTemplate redisTemplate;

    /**
     * Cache basic objects, Integer, String, entity classes, etc.
     *
     * @param key cache key value
     * @param value cached value
     * @return cached object
     */
    public <T> ValueOperations<String, T> setCacheObject(String key, T value) {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        operation.set(key, value);
        return operation;
    }

    /**
     * Cache basic objects, Integer, String, entity classes, etc.
     *
     * @param key cache key value
     * @param value cached value
     * @param timeout time
     * @param timeUnit time granularity
     * @return cached object
     */
    public <T> ValueOperations<String, T> setCacheObject(String key, T value, Integer timeout, TimeUnit timeUnit) {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        operation.set(key, value, timeout, timeUnit);
        return operation;
    }

    /**
     * Get the basic object of the cache.
     *
     * @param key cache key value
     * @return data corresponding to cache key
     */
    public <T> T getCacheObject(String key) {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        return operation.get(key);
    }

    /**
     * Delete a single object
     *
     * @param key
     */
    public void deleteObject(String key) {
        redisTemplate.delete(key);
    }

    /**
     * Delete collection object
     *
     * @param collection
     */
    public void deleteObject(Collection collection) {
        redisTemplate.delete(collection);
    }

    /**
     * Cache List data
     *
     * @param key cache key value
     * @param dataList List data to be cached
     * @return cached object
     */
    public <T> ListOperations<String, T> setCacheList(String key, List<T> dataList) {
        ListOperations listOperation = redisTemplate.opsForList();
        if (null != dataList) {
            int size = dataList.size();
            for (int i = 0; i < size; i++) {
                listOperation.leftPush(key, dataList.get(i));
            }
        }
        return listOperation;
    }

    /**
     * Get the cached list object
     *
     * @param key cache key value
     * @return data corresponding to cache key
     */
    public <T> List<T> getCacheList(String key) {
        List<T> dataList = new ArrayList<T>();
        ListOperations<String, T> listOperation = redisTemplate.opsForList();
        Long size = listOperation.size(key);

        for (int i = 0; i < size; i++) {
            dataList.add(listOperation.index(key, i));
        }
        return dataList;
    }

    /**
     * Cache Set
     *
     * @param key cache key value
     * @param dataSet cached data
     * @return cached data object
     */
    public <T> BoundSetOperations<String, T> setCacheSet(String key, Set<T> dataSet) {
        BoundSetOperations<String, T> setOperation = redisTemplate.boundSetOps(key);
        Iterator<T> it = dataSet.iterator();
        while (it.hasNext()) {
            setOperation.add(it.next());
        }
        return setOperation;
    }

    /**
     * Get cached set
     *
     * @param key
     * @return
     */
    public <T> Set<T> getCacheSet(String key) {
        Set<T> dataSet = new HashSet<T>();
        BoundSetOperations<String, T> operation = redisTemplate.boundSetOps(key);
        dataSet = operation.members();
        return dataSet;
    }

    /**
     * CacheMap
     *
     * @param key
     * @param dataMap
     * @return
     */
    public <T> HashOperations<String, String, T> setCacheMap(String key, Map<String, T> dataMap) {
        HashOperations hashOperations = redisTemplate.opsForHash();
        if (null != dataMap) {
            for (Map.Entry<String, T> entry : dataMap.entrySet()) {
                hashOperations.put(key, entry.getKey(), entry.getValue());
            }
        }
        return hashOperations;
    }

    /**
     * Get the cached Map
     *
     * @param key
     * @return
     */
    public <T> Map<String, T> getCacheMap(String key) {
        Map<String, T> map = redisTemplate.opsForHash().entries(key);
        return map;
    }

    /**
     * Get a list of cached basic objects
     *
     * @param pattern string prefix
     * @return object list
     */
    public Collection<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    /**
     * Update expiration time
     *
     * @param key cache key value
     * @param timeout time
     * @param timeUnit time granularity
     * @return success
     */
    public boolean expire(String key, Integer timeout, TimeUnit timeUnit) {
        return redisTemplate.expire(key, timeout, timeUnit);
    }


}