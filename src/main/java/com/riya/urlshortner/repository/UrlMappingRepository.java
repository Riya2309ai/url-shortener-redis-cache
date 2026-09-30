package com.riya.urlshortner.repository;

import com.riya.urlshortner.entity.UrlMapping;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface UrlMappingRepository extends JpaRepository<UrlMapping, String> {

    @Modifying
    @Transactional
    @Query("""
            UPDATE UrlMapping u
            SET u.clickCount= u.clickCount + :delta
            WHERE u.shortCode= :code    
    """)
    int addClicks(@Param("code") String code, @Param("delta") long delta);


    @Modifying
    @Transactional
    @Query(value = "INSERT INTO url_mapping (short_code, long_url, created_at, expiry_at, click_count) " +
            "VALUES (:shortCode, :longUrl, :createdAt, :expiryAt, :clickCount)", nativeQuery = true)
    void insertNew(@Param("shortCode") String shortCode, @Param("longUrl") String longUrl,
                   @Param("createdAt") java.time.LocalDateTime createdAt,
                   @Param("expiryAt") java.time.LocalDateTime expiryAt,
                   @Param("clickCount") Long clickCount);

}
