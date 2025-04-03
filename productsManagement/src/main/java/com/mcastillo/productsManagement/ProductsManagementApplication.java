package com.mcastillo.productsManagement;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ProductsManagementApplication {


	public static void main(String[] args) {
		SpringApplication.run(ProductsManagementApplication.class, args);
	}


}
