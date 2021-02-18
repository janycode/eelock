package com.example.eelock.controller;

import com.example.eelock.anno.RedisLock;
import com.example.eelock.pojo.Userinfo;
import com.example.eelock.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * @Class: TestRedisLock
 * @Description:
 * @Author: Jerry(姜源)
 * @Create: 21/02/18 11:04
 */
@RestController
@RequestMapping("api/redislock")
public class TestEeLock {
    @Autowired
    private RedisService redisService;

    @RedisLock(key = "REDISLOCK_TEST")
    @GetMapping("test")
    public String test() {
        return UUID.randomUUID().toString();
    }

    @PostMapping("testlock1")
    @RedisLock(key = "#args[1]")
    public String testLock1(Userinfo userinfo, String testKey) {
        System.out.println("[分布式锁]测试1：" + userinfo.getName());
        return userinfo.getName();
    }

    @PostMapping("testlock2")
    @RedisLock(key = "#args[0].getName")
    public String testLock2(Userinfo userinfo, String testKey) {
        System.out.println("[分布式锁]测试2：" + userinfo.getName());
        return userinfo.getName();
    }
}
