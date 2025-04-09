package com.mcastillo.productsManagement.exception;

import com.mcastillo.productsManagement.controller.TestController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(controllers = TestController.class)
public class ExceptionHandlerTest {

	@Autowired
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		// Setup code if needed before each test
	}

	@Test
	void handleTimeoutException_ShouldReturnRequestTimeoutStatus() throws Exception {
		mockMvc.perform(get("/timeout"))
				.andExpect(status().isRequestTimeout())
				.andExpect(content().string("{\"message\":\"Timeout Exception\"}"));
	}

	@Test
	void handleJsonProcessingException_ShouldReturnInternalServerErrorStatus() throws Exception {
		mockMvc.perform(get("/jsonError"))
				.andExpect(status().isInternalServerError())
				.andExpect(content().string("{\"message\":\"JsonProcessing Exception\"}"));
	}
}
