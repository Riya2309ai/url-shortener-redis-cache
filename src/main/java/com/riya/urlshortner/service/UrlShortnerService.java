package com.riya.urlshortner.service;

import com.riya.urlshortner.dto.ShortenRequest;
import com.riya.urlshortner.dto.ShortenResponse;
import com.riya.urlshortner.entity.IdSequence;
import com.riya.urlshortner.entity.UrlMapping;
import com.riya.urlshortner.exception.AliasTakenException;
import com.riya.urlshortner.repository.IdSequenceRepository;
import com.riya.urlshortner.repository.UrlMappingRepository;
import com.riya.urlshortner.util.Base62Encoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;

@Service
public class UrlShortnerService {
    private static final Set<String> RESERVED_ALIASES = Set.of("api", "favicon.ico", "robots.txt", "health");
    private static final String CACHE_PREFIX="url:";

    private final IdSequenceRepository idSequenceRepository;
    private final UrlMappingRepository urlMappingRepository;
    private final StringRedisTemplate stringRedisTemplate;


    @Value("${app.base-url}")
    private String baseUrl;

    public UrlShortnerService(IdSequenceRepository idSequenceRepository, UrlMappingRepository urlMappingRepository, StringRedisTemplate stringRedisTemplate) {
        this.idSequenceRepository = idSequenceRepository;
        this.urlMappingRepository = urlMappingRepository;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public ShortenResponse createShortUrl(ShortenRequest shortenRequest) {
        String shortCode=(shortenRequest.getCustomAlias()!=null && !shortenRequest.getCustomAlias().isBlank()) ?
                reserveCustomAlias(shortenRequest.getCustomAlias()) : generateShortCode();

        UrlMapping urlMapping=UrlMapping.builder()
                .shortCode(shortCode)
                .longUrl(shortenRequest.getLongUrl())
                .createdAt(LocalDateTime.now())
                .expiryAt(shortenRequest.getExpiryAt())
                .clickCount(0L)
                .build();

        cacheUrl(shortCode, shortenRequest.getLongUrl(), shortenRequest.getExpiryAt());

        try {
            urlMappingRepository.insertNew(urlMapping.getShortCode(), urlMapping.getLongUrl(),
                    urlMapping.getCreatedAt(), urlMapping.getExpiryAt(), urlMapping.getClickCount());
        } catch (DataIntegrityViolationException e) {
            throw new AliasTakenException(shortCode);
        }
        return new ShortenResponse(shortCode, baseUrl +"/"+shortCode, shortenRequest.getLongUrl(), shortenRequest.getExpiryAt());
    }

    public String reserveCustomAlias(String alias) {
        if(RESERVED_ALIASES.contains(alias.toLowerCase())){
            throw new AliasTakenException(alias);
        }
        if(urlMappingRepository.existsById(alias)){
            throw new AliasTakenException(alias);
        }
        return alias;
    }

    public String generateShortCode(){
        String shortCode;
        do{
            IdSequence idSequence = idSequenceRepository.save(new IdSequence());
            shortCode = Base62Encoder.encode(idSequence.getId());
        }while(urlMappingRepository.existsById(shortCode));
        return shortCode;
    }

    private void cacheUrl(String shortUrl, String longUrl, LocalDateTime expiryAt){
        String key = CACHE_PREFIX + shortUrl;
        if(expiryAt==null){ //we'll reject this later in validation part
            stringRedisTemplate.opsForValue().set(key, longUrl);
            return ;
        }
        long ttlSeconds= Duration.between(LocalDateTime.now(),expiryAt).toSeconds();
        if(ttlSeconds>0){
            stringRedisTemplate.opsForValue().set(key, longUrl, Duration.ofSeconds(ttlSeconds));
        }
    }

}
