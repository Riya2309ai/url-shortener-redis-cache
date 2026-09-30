package com.riya.urlshortner.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler{

    @ExceptionHandler(AliasTakenException.class)
    public ResponseEntity<String> handleAliasTaken(AliasTakenException ex){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(UrlNotFoundException.class)
    public ResponseEntity<String> handleUrlNotFound(UrlNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
