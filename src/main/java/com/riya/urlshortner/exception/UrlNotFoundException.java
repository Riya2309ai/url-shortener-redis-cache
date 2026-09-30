package com.riya.urlshortner.exception;

public class UrlNotFoundException extends RuntimeException {
    public UrlNotFoundException(String message) {
        super("Short URL not found or expired: " +message);
    }
}
