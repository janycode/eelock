package com.example.eelock.service;

/**
 * @Class: RedisServiceImpl
 * @Description:
 * @Author: Jerry(姜源)
 * @Create: 21/02/18 10:42
 */
public interface RedisService {
    /**
     *  加锁
     * @param key redis key
     * @param value redis value
     * @param expireTime 过期时间
     * @param timeout 获取不到锁超时时间
     * @param interval 重试间隔
     * @return
     */
    boolean tryLock(String key, String value, long expireTime, long timeout, long interval);

    /**
     * 解锁
     * @param key
     * @param value
     */
    void unLock(String key, String value);
}