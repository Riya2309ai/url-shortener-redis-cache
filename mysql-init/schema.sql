CREATE DATABASE IF NOT EXISTS urlshortner_db;
USE urlshortner_db;

CREATE TABLE url_mapping(
    short_code VARCHAR(10) PRIMARY KEY,
    long_url VARCHAR(2048) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expiry_at TIMESTAMP NULL,
    click_count BIGINT DEFAULT 0
);

CREATE TABLE id_sequence(
id BIGINT AUTO_INCREMENT PRIMARY KEY
);