package com.riya.urlshortner.service;

import com.riya.urlshortner.entity.UrlMapping;
import com.riya.urlshortner.exception.UrlNotFoundException;
import com.riya.urlshortner.repository.UrlMappingRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class UrlRedirectService {

    private static final String CACHE_PREFIX = "url:";

    private final UrlMappingRepository urlMappingRepository;
    private final StringRedisTemplate stringRedisTemplate;

    public UrlRedirectService(UrlMappingRepository urlMappingRepository, StringRedisTemplate stringRedisTemplate) {
        this.urlMappingRepository = urlMappingRepository;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public String resolve(String shortCode){
        String key = CACHE_PREFIX + shortCode;

        String cached = stringRedisTemplate.opsForValue().get(key);

        // 1. Cache hit: no database involve
        if(cached!=null){
            return  cached;
        }
        //2. 2. Cache miss: fall back to MySQL
        UrlMapping urlMapping = urlMappingRepository.findById(shortCode).orElseThrow(()->new UrlNotFoundException(shortCode));
        LocalDateTime expiryAt =urlMapping.getExpiryAt();

        if(expiryAt!=null && !expiryAt.isAfter(LocalDateTime.now())){   //expiry was before the current time of now
            throw new  UrlNotFoundException(shortCode);
        }

        // 3. Backfill the cache so the next request is a hit
        if (expiryAt == null) {
            stringRedisTemplate.opsForValue().set(key, urlMapping.getLongUrl());
        } else {
            long ttlSeconds= Duration.between(LocalDateTime.now(),expiryAt).getSeconds();
            stringRedisTemplate.opsForValue().set(key, urlMapping.getLongUrl(),Duration.ofSeconds(ttlSeconds).toSeconds());
        }
        return urlMapping.getLongUrl();
    }

}
