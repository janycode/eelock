package com.example.eelock.util;

import java.util.HashMap;
import java.util.Map;

public class ThreadLocalUtil {

    private static final ThreadLocal<Object> tlContext = new ThreadLocal<>();

    /**
     * 放入缓存
     *
     * @param key   键
     * @param value 数值
     */
    public static void put(Object key, Object value) {
        Map m = (Map) tlContext.get();
        if (m == null) {
            m = new HashMap();
            tlContext.set(m);
        }
        m.put(key, value);
    }

    /**
     * 获取缓存
     *
     * @param key 键
     */
    public static Object get(Object key) {
        Map m = (Map) tlContext.get();
        if (m == null) return null;
        return m.get(key);
    }

    /**
     * 清理
     *
     * @param key 键
     */
    public static void clear(Object key) {
        Map m = (Map) tlContext.get();
        if (m == null) return;
        m.remove(key);
    }
}