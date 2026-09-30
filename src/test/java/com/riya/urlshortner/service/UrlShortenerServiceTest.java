package com.riya.urlshortner.service;

import com.riya.urlshortner.dto.ShortenRequest;
import com.riya.urlshortner.entity.IdSequence;
import com.riya.urlshortner.exception.AliasTakenException;
import com.riya.urlshortner.repository.IdSequenceRepository;
import com.riya.urlshortner.repository.UrlMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlShortenerServiceTest {

    @Mock private UrlMappingRepository urlMappingRepository;
    @Mock private IdSequenceRepository idSequenceRepository;
    @Mock private StringRedisTemplate stringRedisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

    private UrlShortnerService service;

    @BeforeEach
    void setUp() {
        service = new UrlShortnerService(idSequenceRepository, urlMappingRepository, stringRedisTemplate);
        ReflectionTestUtils.setField(service, "baseUrl", "http://localhost:8080");
    }

    @Test
    void generatesBase62CodeWhenNoCustomAliasGiven() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        IdSequence seq = new IdSequence();
        ReflectionTestUtils.setField(seq, "id", 10L);
        when(idSequenceRepository.save(any())).thenReturn(seq);
        when(urlMappingRepository.existsById(anyString())).thenReturn(false);

        ShortenRequest request = new ShortenRequest();
        request.setLongUrl("https://example.com");

        var response = service.createShortUrl(request);

        assertEquals("a", response.getShortCode()); // Base62Encoder.encode(10) == "a"
        verify(urlMappingRepository).insertNew(eq("a"), eq("https://example.com"), any(), isNull(), eq(0L));
    }

    @Test
    void usesCustomAliasWhenProvidedAndAvailable() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(urlMappingRepository.existsById("myalias")).thenReturn(false);

        ShortenRequest request = new ShortenRequest();
        request.setLongUrl("https://example.com");
        request.setCustomAlias("myalias");

        var response = service.createShortUrl(request);

        assertEquals("myalias", response.getShortCode());
        verifyNoInteractions(idSequenceRepository); // custom alias must never touch the counter
    }

    @Test
    void throwsWhenCustomAliasAlreadyTaken() {
        when(urlMappingRepository.existsById("taken")).thenReturn(true);

        ShortenRequest request = new ShortenRequest();
        request.setLongUrl("https://example.com");
        request.setCustomAlias("taken");

        assertThrows(AliasTakenException.class, () -> service.createShortUrl(request));
        verifyNoInteractions(idSequenceRepository);
    }

    @Test
    void throwsWhenCustomAliasIsReservedWord() {
        ShortenRequest request = new ShortenRequest();
        request.setLongUrl("https://example.com");
        request.setCustomAlias("api");

        assertThrows(AliasTakenException.class, () -> service.createShortUrl(request));
    }
}