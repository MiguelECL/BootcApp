package com.mcastillo.productsManagement.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mcastillo.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeoutException;

public interface ProductsManagementServiceInterface {

	Logger logger = LoggerFactory.getLogger("logger");

	String getProducts() throws TimeoutException;
	void createProduct(Product product) throws TimeoutException, JsonProcessingException;
	void updateProduct(Product product) throws TimeoutException, JsonProcessingException;
	void deleteProduct(int id) throws TimeoutException, JsonProcessingException;
}
