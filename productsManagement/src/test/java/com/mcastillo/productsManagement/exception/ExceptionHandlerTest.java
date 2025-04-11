package com.mcastillo.productsManagement.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcastillo.Product;
import com.mcastillo.productsManagement.controller.ProductsManagementController;
import com.mcastillo.productsManagement.service.impl.ProductsManagementServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ProductsManagementController.class, ExceptionHandler.class})
public class ExceptionHandlerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ProductsManagementServiceImpl productsManagementService;

	@Test
	public void testRuntimeExceptionHandling() throws Exception {
		when(productsManagementService.getProducts()).thenThrow(new RuntimeException("Timeout occurred"));

		mockMvc.perform(get("/products"))
				.andExpect(status().isRequestTimeout());
	}

	@Test
	public void testIllegalArgumentExceptionHandling() throws Exception {
		Product mockProduct = new Product(1,"test", "test", 10.0f, Date.valueOf("2024-10-2"));

		ObjectMapper objectMapper = new ObjectMapper();
		String productJson = objectMapper.writeValueAsString(mockProduct);

		when(productsManagementService.createProduct(any())).thenThrow(new IllegalArgumentException("JSON Error occurred"));

		mockMvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content(productJson))
				.andExpect(status().is(HttpStatus.INTERNAL_SERVER_ERROR.value()));

	}
}