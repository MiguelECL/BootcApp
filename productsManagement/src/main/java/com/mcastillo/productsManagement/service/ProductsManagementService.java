package com.mcastillo.productsManagement.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mcastillo.Product;

import java.util.List;
import java.util.concurrent.TimeoutException;

public interface ProductsManagementService {

	List<Product> getProducts() throws TimeoutException;
	Product createProduct(Product product) throws TimeoutException, JsonProcessingException;
	Product updateProduct(Product product) throws TimeoutException, JsonProcessingException;
	void deleteProduct(int id) throws TimeoutException, JsonProcessingException;
}
