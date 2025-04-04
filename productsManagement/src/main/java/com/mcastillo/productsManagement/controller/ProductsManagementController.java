package com.mcastillo.productsManagement.controller;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mcastillo.Product;
import com.mcastillo.productsManagement.service.ProductsManagementService;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeoutException;

@RestController
@CrossOrigin
public class ProductsManagementController {

  ProductsManagementService service = new ProductsManagementService();

  @GetMapping("/products")
  public String getProducts() {

    try {
      return service.getProducts();
    } catch (TimeoutException e){
      System.out.println("TimeoutException: " + e.getMessage());
      return "TimeoutException: " + e.getMessage();
    }
  }

  @PostMapping("/products")
  public String createProduct(@RequestBody Product product) {
    try {
      service.createProduct(product);
      return "Product created";
    } catch (TimeoutException e){
      System.out.println("TimeoutException: " + e.getMessage());
      return "TimeoutException: " + e.getMessage();
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @PutMapping("/products/{id}")
  public String updateProduct(@PathVariable String id, @RequestBody Product product) {
    try {
      service.updateProduct(product);
    } catch (TimeoutException e){
      System.out.println("TimeoutException: " + e.getMessage());
      return "TimeoutException: " + e.getMessage();
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    return "Product updated with id: " + id;
  }

  @DeleteMapping("/products/{id}")
  public String deleteProduct(@PathVariable int id) {
    try {
      service.deleteProduct(id);
      return "Product deleted with id: " + id;
    } catch (TimeoutException e){
      System.out.println("TimeoutException: " + e.getMessage());
      return "Error Deleting Product";
    }
  }
}
