package com.example.rqchallenge.exception.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.example.rqchallenge.exception.TooManyRequestsException;

@ControllerAdvice
public class RestResponseEntityExceptionHandler 
  extends ResponseEntityExceptionHandler {
    @ExceptionHandler(value 
      = {TooManyRequestsException.class})
    protected ResponseEntity<Object> handleTooManyRequests(
      RuntimeException ex, WebRequest request) {
        Map<String, List<String>> body = new HashMap<>();
        List<String> details = new ArrayList<>();
        details.add(ex.toString());
        body.put("errors", details);
        return new ResponseEntity<>(body, HttpStatus.TOO_MANY_REQUESTS);
    }
}