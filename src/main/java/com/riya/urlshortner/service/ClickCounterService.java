package com.riya.urlshortner.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class ClickCounterService {

    public static final String COUNT_PREFIX="clickcount:";
    public static final String PENDING_KEY="click-pending:";

    private final StringRedisTemplate stringRedisTemplate;

    public ClickCounterService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }
    public void recordClick(String shortCode){
        stringRedisTemplate.opsForValue().increment(COUNT_PREFIX+shortCode);
        stringRedisTemplate.opsForSet().add(PENDING_KEY, shortCode);
    }


}
