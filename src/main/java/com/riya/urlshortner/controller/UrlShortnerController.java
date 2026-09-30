package com.riya.urlshortner.controller;

import com.riya.urlshortner.dto.ShortenRequest;
import com.riya.urlshortner.dto.ShortenResponse;
import com.riya.urlshortner.service.UrlShortnerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UrlShortnerController {

    private final UrlShortnerService urlShortnerService;

    public UrlShortnerController(UrlShortnerService urlShortnerService) {
        this.urlShortnerService = urlShortnerService;
    }

    @PostMapping("/shorten")
    public ResponseEntity<ShortenResponse> shortenUrl(@Valid @RequestBody ShortenRequest shortenRequest) {
        ShortenResponse shortenResponse= urlShortnerService.createShortUrl(shortenRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(shortenResponse);
    }


}
