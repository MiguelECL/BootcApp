package com.mcastillo.productsManagement.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeoutException;

@RestController
public class TestController {

	@GetMapping("/timeout")
	public void timeoutEndpoint() throws TimeoutException {
		throw new TimeoutException("Timeout occurred");
	}

	@GetMapping("/jsonError")
	public void jsonErrorEndpoint() throws JsonProcessingException {
		throw new JsonProcessingException("Error processing JSON") {};
	}
}