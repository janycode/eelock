package com.example.eelock.advice;

import com.example.eelock.anno.RedisLock;
import com.example.eelock.service.RedisService;
import com.example.eelock.util.ThreadLocalUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * 分布式锁AOP
 */
@Aspect
@Component
public class LockAspect {

    @Autowired
    private RedisService redisService;

    /**
     * 环绕通知  加锁 解锁
     * 注意：@Around("@annotation(RedisLock)") 会报错
     *     java.lang.IllegalArgumentException: error Type referred to is not an annotation
     *
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("@annotation(com.example.eelock.anno.RedisLock)")
    public Object redisLockAop(ProceedingJoinPoint joinPoint) throws Throwable {
        Object res = null;
        RedisLock lock = ((MethodSignature) joinPoint.getSignature()).getMethod().getAnnotation(RedisLock.class);
        String uuid = UUID.randomUUID().toString();
        String key = getKey(joinPoint, lock.key());
        System.err.println("[KEY] :" + key);
        if (ThreadLocalUtil.get(key) != null) {
            //当前线程已经获取到锁 不需要重复获取锁。保证可重入性
            return joinPoint.proceed();
        }
        if (redisService.tryLock(key, uuid, lock.expireTime(), lock.timeout(), lock.interval())) {
            //获取到锁进行标记 执行方法
            ThreadLocalUtil.put(key, "");
            res = joinPoint.proceed();
            //方法执行结束 释放锁
            ThreadLocalUtil.clear(key);
            redisService.unLock(key, uuid);
            return res;
        } else {
            //获取不到锁 抛出异常 进入统一异常处理
            throw new Exception();
        }
    }

    /**
     * 根据参数 和注解 获取 redis key值
     *
     * @param joinPoint
     * @param key
     * @return
     */
    public String getKey(ProceedingJoinPoint joinPoint, String key) {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        try {
            if ("".equals(key)) {
                //默认类名 + 方法名
                return className + methodName;
            }
            if (key.startsWith("#args")) {
                //包含 #args 读取参数 设置key 不包含直接返回
                //获取参数
                Object[] args = joinPoint.getArgs();
                //获取注解下标  例如：#args[0]  或者 #args[1].getName()
                int index = Integer.parseInt(key.substring(key.indexOf("[") + 1, key.indexOf("]")));
                Object keyArgs = args[index];
                if (key.split("\\.").length <= 1) {
                    return keyArgs.toString();
                }
                //反射执行方法 拿到返回值 返回key
                Class<?> clas = keyArgs.getClass();
                Method method = clas.getMethod(key.split("\\.")[1].split("\\(")[0]);
                return method.invoke(keyArgs).toString();
            }
            return key;
        } catch (Exception e) {
            return className + methodName;
        }
    }
}