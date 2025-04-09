package com.mcastillo.productsManagement.exception;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.util.concurrent.TimeoutException;

@ControllerAdvice
public class ExceptionHandler {

	@org.springframework.web.bind.annotation.ExceptionHandler(TimeoutException.class)
	public ResponseEntity<String> handleTimeoutException(TimeoutException e) {
		return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("TimeoutException: " + e.getMessage());
	}

	@org.springframework.web.bind.annotation.ExceptionHandler(JsonProcessingException.class)
	public ResponseEntity<String> handleJSONProcessingException(JsonProcessingException e) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("JSONProcessingException: " + e.getMessage());
	}
}
