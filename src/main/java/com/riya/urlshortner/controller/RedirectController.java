package com.riya.urlshortner.controller;

import com.riya.urlshortner.service.ClickCounterService;
import com.riya.urlshortner.service.UrlRedirectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class RedirectController {

    private final ClickCounterService clickCounterService;

    private final UrlRedirectService urlRedirectService;

    public RedirectController(ClickCounterService clickCounterService, UrlRedirectService urlRedirectService) {
        this.clickCounterService = clickCounterService;
        this.urlRedirectService = urlRedirectService;
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@Valid @PathVariable String shortCode) {
        String longUrl= urlRedirectService.resolve(shortCode);
        clickCounterService.recordClick(shortCode);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(longUrl))
                .build();

    }
}
