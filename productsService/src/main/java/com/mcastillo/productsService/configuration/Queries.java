package com.mcastillo.productsService.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "queries")
public class Queries {
    private String selectAllProducts;

    public String getSelectAllProducts() {
        return selectAllProducts;
    }

    public void setSelectAllProducts(String selectAllProducts) {
        this.selectAllProducts = selectAllProducts;
    }
}
