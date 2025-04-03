package com.mcastillo.productsService;

import com.mcastillo.productsService.service.ProductsServiceService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import repository.ProductsServiceRepository;

@SpringBootApplication
public class ProductsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductsServiceApplication.class, args);

		// Initialize the Repository
		ProductsServiceRepository repository = new ProductsServiceRepository();
		repository.connectDatabase();

		// Initialize the Service
		ProductsServiceService service = new ProductsServiceService();
		service.pollQueue();

	}

}
