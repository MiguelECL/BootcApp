package com.mcastillo.productsManagement.exception;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mcastillo.DatabaseException;
import com.mcastillo.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.util.concurrent.TimeoutException;

@ControllerAdvice
public class ExceptionHandler {

    Logger logger = LoggerFactory.getLogger("ControllerLogger");


    @org.springframework.web.bind.annotation.ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Response> handleJSONParseException(HttpMessageNotReadableException e) {
        logger.error("JSONParse Exception!", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response("JSON Parse Exception"));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Response> handleTimeoutException(RuntimeException e) {
        logger.error("TimeoutException!", e);
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(new Response("Timeout Error"));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Response> handleJSONProcessingException(IllegalArgumentException e) {
        logger.error("JsonProcessingException!", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response("Error while processing JSON"));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(DatabaseException.class)
    public ResponseEntity<Response> handleDatabaseException(DatabaseException e) {
        logger.error("DatabaseException!", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(e.getMessage()));
    }

	@org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
	public ResponseEntity<Response> handleException(Exception e){
		logger.error("Exception!", e);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body((new Response("Exception")));
	}
}
