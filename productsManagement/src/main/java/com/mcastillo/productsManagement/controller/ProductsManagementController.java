package com.mcastillo.productsManagement.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mcastillo.Product;
import com.mcastillo.productsManagement.service.ProductsManagementService;
import com.mcastillo.productsManagement.service.impl.ProductsManagementServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeoutException;

@RestController
// @CrossOrigin annotation not really needed anymore due to the fact that the frontend is now
// being served by springboot
@CrossOrigin
public class ProductsManagementController {

	private final ProductsManagementService productsManagementService;

	public ProductsManagementController(ProductsManagementService productsManagementService) {
		this.productsManagementService = productsManagementService;
	}

	@GetMapping("/products")
	public ResponseEntity<List<Product>> getProducts() throws TimeoutException {
		return ResponseEntity.ok(productsManagementService.getProducts());
	}

	@PostMapping("/products")
	public ResponseEntity<Product> createProduct(@RequestBody Product product) throws JsonProcessingException, TimeoutException {
		Product response = productsManagementService.createProduct(product);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PutMapping("/products/{id}")
	public ResponseEntity<Product> updateProduct(@PathVariable String id, @RequestBody Product product) throws JsonProcessingException, TimeoutException {
		Product response = productsManagementService.updateProduct(product);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@DeleteMapping("/products/{id}")
	public ResponseEntity<Void> deleteProduct(@PathVariable int id) throws TimeoutException, JsonProcessingException {
		productsManagementService.deleteProduct(id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
	}
}
