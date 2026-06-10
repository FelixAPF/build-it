package com.build_it.buildit.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  // Catch all standard RuntimeExceptions (like "Email already in use")
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
    // Strip the stack trace and return ONLY the clean message string
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
  }

  // Catch specific HTTP status exceptions (like Unauthorized logins)
  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<String> handleResponseStatusException(ResponseStatusException ex) {
    return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
  }
}
