package com.example.eelock.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * redis 分布式锁注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RedisLock {

    /**
     * key 默认为类名+方法名
     * 使用方法：
     * 1.String 字符串
     * 2.#args[]变量
     * 例如： #args[0]
     * #args[1].getName() 只支持无参方法调用
     */
    String key() default "";

    /**
     * 重新获取锁的间隔时间，默认100ms
     */
    long interval() default 100L;

    /**
     * 失效时间，默认10秒
     */
    long expireTime() default 10 * 1000L;

    /**
     * 阻塞时间，超时获取不到锁，抛异常 或走回调方法
     */
    long timeout() default 5 * 1000L;
}