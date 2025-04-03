package com.mcastillo.productsManagement.controller;

import com.mcastillo.productsManagement.service.ProductsManagementService;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
public class ProductsManagementController {

  ProductsManagementService service = new ProductsManagementService();

  @GetMapping("/products")
  public String getProducts() {

    service.getProducts();
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
