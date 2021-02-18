package com.example.eelock.service.impl;

import com.example.eelock.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * @Class: RedisServiceImpl
 * @Description:
 * @Author: Jerry(姜源)
 * @Create: 21/02/18 10:42
 */
@Service
@SuppressWarnings("all")
public class RedisServiceImpl implements RedisService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 加锁
     *
     * @param key        redis key
     * @param value      redis value
     * @param expireTime 过期时间
     * @param timeout    获取不到锁超时时间
     * @param interval   重试间隔
     * @return
     */
    @Override
    public boolean tryLock(String key, String value, long expireTime, long timeout, long interval) {
        if (interval <= 0) {
            //默认等待时间 30 毫秒
            interval = 30L;
        }
        try {
            if (timeout > 0) {
                long begin = System.currentTimeMillis();
                while (System.currentTimeMillis() - begin < timeout) {
                    if (redisTemplate.opsForValue().setIfAbsent(key, value, expireTime, TimeUnit.MILLISECONDS)) {
                        return true;
                    }
                    //等待
                    synchronized (Thread.currentThread()) {
                        Thread.currentThread().wait(interval);
                    }
                }
                return false;
            } else {
                return redisTemplate.opsForValue().setIfAbsent(key, value, expireTime, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 解锁
     *
     * @param key
     * @param value
     */
    @Override
    public void unLock(String key, String value) {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        DefaultRedisScript<Long> defaultRedisScript = new DefaultRedisScript();
        defaultRedisScript.setScriptText(script);
        defaultRedisScript.setResultType(Long.class);
        //执行 脚本 删除 key ,必须使用lua 脚本实现  保证原子性
        Long res = (Long) redisTemplate.execute(defaultRedisScript, Collections.singletonList(key), value);
        if (res != 1L) {
            System.err.println("释放失败");
        }
    }
}
