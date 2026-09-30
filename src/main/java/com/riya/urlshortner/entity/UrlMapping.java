package com.riya.urlshortner.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "url_mapping")
public class UrlMapping {

    @Id
    @Column(name = "shortCode", length = 10)
    private String shortCode;

    @Column(name = "long_url", nullable = false, length = 2048)
    private String longUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "expiry_at")
    private LocalDateTime expiryAt;

    @Column(name = "click_count")
    private Long clickCount;



}
