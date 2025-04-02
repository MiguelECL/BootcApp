package com.mcastillo.productsService.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
public class ProductsServiceController {

    @PostMapping("/products")
    public String createProduct(@RequestBody String product){
        return "hello " + product;
    }

    @GetMapping("/products")
    public String getProducts(){
        return "Hello";
    }

    @DeleteMapping("/products/{id}")
    public String deleteProduct(@PathVariable int id){
        return "hello " + id;
    }

    @PutMapping("/products/{id}")
    public String updateProduct(@PathVariable int id){
        return "hello " + id;
    }

}
