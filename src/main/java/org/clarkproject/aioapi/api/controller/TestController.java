package org.clarkproject.aioapi.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @GetMapping("/Test")
    public String test() {
        redisTemplate.opsForSet().add("Set", "set");
        return "ADD";
    }
}
