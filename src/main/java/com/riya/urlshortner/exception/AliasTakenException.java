package com.riya.urlshortner.exception;

public class AliasTakenException extends RuntimeException {
    public AliasTakenException(String message) {
        super("Alias already in use: "+message);
    }
}
