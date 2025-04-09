package com.mcastillo.productsManagement.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mcastillo.Product;

import java.util.concurrent.TimeoutException;

public interface ProductsManagementService {

	String getProducts() throws TimeoutException;
	String createProduct(Product product) throws TimeoutException, JsonProcessingException;
	String updateProduct(Product product) throws TimeoutException, JsonProcessingException;
	void deleteProduct(int id) throws TimeoutException, JsonProcessingException;
}
