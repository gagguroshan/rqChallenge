package com.example.rqchallenge.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;

public class TooManyRequestsException extends RuntimeException {
    protected LocalDateTime timestamp;
    protected int status;
    protected String message;	
    public TooManyRequestsException(String message) {
        this.status = HttpStatus.TOO_MANY_REQUESTS.value();
        this.timestamp = LocalDateTime.now();
        this.message = message;
    }
    @Override
    public String toString() {
    	return this.message;
    }
}