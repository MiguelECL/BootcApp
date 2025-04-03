package com.mcastillo.productsManagement.controller;

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
      service.getProducts();
    } catch (TimeoutException e){
      System.out.println("TimeoutException: " + e.getMessage());
    }
    return "Hello World";
  }

  @PostMapping("/products")
  public String createProduct() {
    return "Product created";
  }

  @PutMapping("/products/{id}")
  public String updateProduct(@PathVariable String id) {
    return "Product updated with id: " + id;
  }

  @DeleteMapping("(/products/{id}")
  public String deleteProduct(@PathVariable String id) {
    return "Product deleted with id: " + id;
  }
}
