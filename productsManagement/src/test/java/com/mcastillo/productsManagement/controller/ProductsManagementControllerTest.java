package com.mcastillo.productsManagement.controller;

import com.amazonaws.Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mcastillo.Product;
import com.mcastillo.productsManagement.service.ProductsManagementService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.Date;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class ProductsManagementControllerTest {

  @Mock
  private ProductsManagementService service;

  @InjectMocks
  private ProductsManagementController controller;

  @Test
  void testGetProducts() throws TimeoutException {

    List<Product> mockProducts = Arrays.asList(
      new Product(1, "Product 1", "Description 1", 10.0f, Date.valueOf("2023-4-12")),
      new Product(2, "Product 2", "Description 2", 20.0f, Date.valueOf("2023-4-8"))
    );

    // Mocking service
    when(service.getProducts()).thenReturn(mockProducts.toString());

    // call
    ResponseEntity<?> response = controller.getProducts();

    assertEquals(200, response.getStatusCodeValue());
    assertEquals(mockProducts.toString(), response.getBody());

  }

  @Test
  void testGetProducts_TimeoutException() throws TimeoutException {
    String error = "TimeoutException: Timeout occurred";
    when (service.getProducts()).thenThrow(new TimeoutException(error));

    ResponseEntity<?> response = controller.getProducts();
    assertEquals(HttpStatus.REQUEST_TIMEOUT, response.getStatusCode());

  }

  @Test
  void testCreateProduct() throws TimeoutException, JsonProcessingException {
    Product mockProduct = new Product (1, "Product 1", "Description 1", 10.0f, Date.valueOf("2023-4-12"));

    // call
    ResponseEntity<?> response = controller.createProduct(mockProduct);
    assertEquals(200, response.getStatusCodeValue());
    assertEquals("Product created with id: " + mockProduct.getId(), response.getBody());

  }

  @Test
  void testCreateProduct_TimeoutException() throws JsonProcessingException, TimeoutException {
    String error = "TimeoutException: Timeout occurred";
    Product mockProduct = new Product (1, "Product 1", "Description 1", 10.0f, Date.valueOf("2023-4-12"));

    doThrow(new TimeoutException(error)).when(service).createProduct(mockProduct);

    ResponseEntity<?> response = controller.createProduct(mockProduct);
    assertEquals(HttpStatus.REQUEST_TIMEOUT, response.getStatusCode());
  }

  @Test
  void testCreateProduct_JsonProcessingException() throws JsonProcessingException, TimeoutException {
    Product mockProduct = new Product (1, "Product 1", "Description 1", 10.0f, Date.valueOf("2023-4-12"));

    doThrow(new JsonProcessingException("JsonProcessingException: Error processing JSON"){}).when(service).createProduct(mockProduct);

    ResponseEntity<?> response = controller.createProduct(mockProduct);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void testUpdateProduct() throws JsonProcessingException, TimeoutException {
    Product mockProduct = new Product (1, "Product 1", "Description 1", 10.0f, Date.valueOf("2023-4-12"));

    // call
    ResponseEntity<?> response = controller.updateProduct("1", mockProduct);
    assertEquals(200, response.getStatusCodeValue());
    assertEquals("Product updated with id: " + "1", response.getBody());
  }

  @Test
  void testUpdateProduct_TimeoutException() throws JsonProcessingException, TimeoutException {
    Product mockProduct = new Product (1, "Product 1", "Description 1", 10.0f, Date.valueOf("2023-4-12"));

    doThrow(new TimeoutException("TimeoutException: Timeout occurred")).when(service).updateProduct(mockProduct);
    ResponseEntity<?> response = controller.updateProduct("1", mockProduct);
    assertEquals(HttpStatus.REQUEST_TIMEOUT, response.getStatusCode());
  }

  @Test
  void testUpdateProduct_JsonProcessingException() throws JsonProcessingException, TimeoutException {
    Product mockProduct = new Product (1, "Product 1", "Description 1", 10.0f, Date.valueOf("2023-4-12"));

    doThrow(new JsonProcessingException("JsonProcessingException: Error processing JSON"){}).when(service).updateProduct(mockProduct);
    ResponseEntity<?> response = controller.updateProduct("1", mockProduct);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void testDeleteProduct() {

    int mockId = 1;

    // call
    ResponseEntity<?> response = controller.deleteProduct(mockId);
    assertEquals(200, response.getStatusCodeValue());
    assertEquals("Product deleted with id: " + mockId, response.getBody());

  }

  @Test
  void testDeleteProduct_TimeoutException() throws TimeoutException {

    int mockId = 1;

    doThrow(new TimeoutException("TimeoutException: Timeout occurred")).when(service).deleteProduct(mockId);

    // call
    ResponseEntity<?> response = controller.deleteProduct(mockId);
    assertEquals(HttpStatus.REQUEST_TIMEOUT, response.getStatusCode());

  }
}
