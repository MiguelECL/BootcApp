package com.mcastillo.productsService.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "queries")
public class Queries {
    private String selectAllProducts;
    private String createProduct;
    private String updateProduct;
    private String deleteProduct;

    public String getSelectAllProducts() {
        return selectAllProducts;
    }

    public void setSelectAllProducts(String selectAllProducts) {
        this.selectAllProducts = selectAllProducts;
    }

    public String getCreateProduct() {
        return createProduct;
    }

    public void setCreateProduct(String insertProduct) {
        this.createProduct = insertProduct;
    }

    public String getUpdateProduct() {
        return updateProduct;
    }

    public void setUpdateProduct(String updateProduct) {
        this.updateProduct = updateProduct;
    }

    public String getDeleteProduct() {
        return deleteProduct;
    }

    public void setDeleteProduct(String deleteProduct) {
        this.deleteProduct = deleteProduct;
    }
}
