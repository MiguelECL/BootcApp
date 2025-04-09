package com.mcastillo.productsManagement.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mcastillo.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeoutException;

public interface ProductsManagementService {

	Logger logger = LoggerFactory.getLogger("logger");

	String getProducts() throws TimeoutException;
	String createProduct(Product product) throws TimeoutException, JsonProcessingException;
	String updateProduct(Product product) throws TimeoutException, JsonProcessingException;
	void deleteProduct(int id) throws TimeoutException, JsonProcessingException;
}
