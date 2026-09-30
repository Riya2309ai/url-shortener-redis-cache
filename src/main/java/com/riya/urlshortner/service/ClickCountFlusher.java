package com.riya.urlshortner.service;

import com.riya.urlshortner.repository.UrlMappingRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ClickCountFlusher {
    private final StringRedisTemplate stringRedisTemplate;
    private final UrlMappingRepository urlMappingRepository;

    public ClickCountFlusher(StringRedisTemplate stringRedisTemplate, UrlMappingRepository urlMappingRepository) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.urlMappingRepository = urlMappingRepository;
    }

    @Scheduled(fixedDelay = 5000)
    public void flush(){
        String code ;
        while((code = stringRedisTemplate.opsForSet().pop(ClickCounterService.PENDING_KEY))!=null){
            String key=ClickCounterService.COUNT_PREFIX + code;
            String value=stringRedisTemplate.opsForValue().getAndDelete(key);  //getAndDelete() reads the count and resets it in one atomic step.
            if(value==null){
                continue;
            }
            long delta=Long.parseLong(value);
            try{
                urlMappingRepository.addClicks(code, delta);
            }
            catch (Exception ex){
                stringRedisTemplate.opsForValue().increment(key, delta);
                stringRedisTemplate.opsForSet().add(ClickCounterService.PENDING_KEY, code);
                break;
            }
        }
    }

}
