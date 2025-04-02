package com.mcastillo.productsService.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductsServiceController {

    @GetMapping
    public int getProducts(){
        return 0;
    }

}
