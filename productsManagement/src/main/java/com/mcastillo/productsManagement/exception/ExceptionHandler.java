package com.mcastillo.productsManagement.exception;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mcastillo.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.util.concurrent.TimeoutException;

@ControllerAdvice
public class ExceptionHandler {

	Logger logger = LoggerFactory.getLogger("ControllerLogger");

	@org.springframework.web.bind.annotation.ExceptionHandler(TimeoutException.class)
	public ResponseEntity<Response> handleTimeoutException(TimeoutException e) {
		logger.error("TimeoutException! - {}", e.getMessage());
		return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(new Response("Timeout Exception"));
	}

	@org.springframework.web.bind.annotation.ExceptionHandler(JsonProcessingException.class)
	public ResponseEntity<Response> handleJSONProcessingException(JsonProcessingException e) {
		logger.error("JsonProcessingException! - {}", e.getMessage());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response("JsonProcessing Exception"));
	}
}
