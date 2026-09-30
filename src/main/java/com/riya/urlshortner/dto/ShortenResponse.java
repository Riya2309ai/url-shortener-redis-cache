package com.riya.urlshortner.dto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ShortenResponse {

    private String shortCode;

    private String shortUrl;

    private String longUrl;
    private LocalDateTime expiryAt;

}
