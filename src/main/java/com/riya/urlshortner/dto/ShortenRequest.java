package com.riya.urlshortner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShortenRequest {

    @NotBlank(message = "longurl is required")
    @Pattern(regexp = "^https?://.+", message = "longUrl must start with http:// or https://")
    private String longUrl;

    @Size(max = 10, message = "customAlias must be 10 characters or fewer")
    @Pattern(regexp = "^[a-zA-Z0-9_-]*$", message = "customAlias can only contain letters, numbers, hyphens, and underscores")
    private String customAlias;

    private LocalDateTime expiryAt;

}
