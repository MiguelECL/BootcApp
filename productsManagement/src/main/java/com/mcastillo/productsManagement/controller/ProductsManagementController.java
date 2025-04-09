package com.mcastillo.productsManagement.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mcastillo.Product;
import com.mcastillo.productsManagement.service.ProductsManagementService;
import com.mcastillo.productsManagement.service.ProductsManagementServiceInterface;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeoutException;

@RestController
// @CrossOrigin annotation not really needed anymore due to the fact that the frontend is now
// being served by springboot
@CrossOrigin
public class ProductsManagementController {

	private final ProductsManagementServiceInterface service;

	public ProductsManagementController(ProductsManagementService service) {
		this.service = service;
	}

	@GetMapping("/products")
	public ResponseEntity<?> getProducts() throws TimeoutException {
		return ResponseEntity.ok(service.getProducts());
	}

	@PostMapping("/products")
	public ResponseEntity<?> createProduct(@RequestBody Product product) throws JsonProcessingException, TimeoutException {
			service.createProduct(product);
			return ResponseEntity.ok("Product created with id: " + product.getId());
	}

	@PutMapping("/products/{id}")
	public ResponseEntity<?> updateProduct(@PathVariable String id, @RequestBody Product product) throws JsonProcessingException, TimeoutException {
			service.updateProduct(product);
			return ResponseEntity.ok("Product updated with id: " + id);
	}

	@DeleteMapping("/products/{id}")
	public ResponseEntity<?> deleteProduct(@PathVariable int id) throws TimeoutException, JsonProcessingException {
			service.deleteProduct(id);
			return ResponseEntity.ok("Product deleted with id: " + id);
	}
}
