package com.mcastillo.productsManagement.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mcastillo.Product;
import com.mcastillo.productsManagement.service.ProductsManagementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeoutException;

@RestController
// @CrossOrigin annotation not really needed anymore due to the fact that the frontend is now
// being served by springboot
@CrossOrigin
public class ProductsManagementController {

  private final ProductsManagementService service; // interfaz

  public ProductsManagementController(ProductsManagementService service) {
    this.service = service;
  }

  @GetMapping("/products")
  public ResponseEntity<?> getProducts() {
    // controller advice
    try {
      return ResponseEntity.ok(service.getProducts());
    } catch (TimeoutException e){
      return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("TimeoutException: " + e.getMessage());
    }
  }

  @PostMapping("/products")
  public ResponseEntity<?> createProduct(@RequestBody Product product) {
    try {
      service.createProduct(product);
      return ResponseEntity.ok("Product created with id: " + product.getId());
    } catch (TimeoutException e){
      return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("TimeoutException: " + e.getMessage());
    } catch (JsonProcessingException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("TimeoutException: " + e.getMessage());
    }
  }

  @PutMapping("/products/{id}")
  public ResponseEntity<?> updateProduct(@PathVariable String id, @RequestBody Product product) {
    try {
      service.updateProduct(product);
      return ResponseEntity.ok("Product updated with id: " + id);
    } catch (TimeoutException e){
      return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("TimeoutException: " + e.getMessage());
    } catch (JsonProcessingException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("TimeoutException: " + e.getMessage());
    }
  }

  @DeleteMapping("/products/{id}")
  public ResponseEntity<?> deleteProduct(@PathVariable int id) {
    try {
      service.deleteProduct(id);
      return ResponseEntity.ok("Product deleted with id: " + id);
    } catch (TimeoutException e){
      return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("TimeoutException: " + e.getMessage());
    }
  }
}
