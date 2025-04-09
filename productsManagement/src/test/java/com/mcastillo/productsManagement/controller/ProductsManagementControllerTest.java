package com.mcastillo.productsManagement.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mcastillo.Product;
import com.mcastillo.productsManagement.service.ProductsManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
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
  private ProductsManagementController controller;

  @BeforeEach
  void setUp(){
    // Initialize the controller with the mocked service
    controller = new ProductsManagementController(service);
  }

  @Test
  void testGetProducts() throws TimeoutException {

    List<Product> mockProducts = Arrays.asList(
      new Product(1, "Product 1", "Description 1", 10.0f, Date.valueOf("2023-4-12")),
      new Product(2, "Product 2", "Description 2", 20.0f, Date.valueOf("2023-4-8"))
    );

    when(service.getProducts()).thenReturn(mockProducts.toString());

    ResponseEntity<?> response = controller.getProducts();

    assertEquals(200, response.getStatusCodeValue());
    assertEquals(mockProducts.toString(), response.getBody());

  }

  @Test
  void testGetProducts_TimeoutException() throws TimeoutException {
    when(service.getProducts()).thenThrow(new TimeoutException("Timeout occurred"));

    assertThrows(TimeoutException.class, ()-> controller.getProducts());
  }

  @Test
  void testCreateProduct() throws JsonProcessingException, TimeoutException {
    Product mockProduct = new Product (1, "Product 1", "Description 1", 10.0f, Date.valueOf("2023-4-12"));

    ResponseEntity<?> response = controller.createProduct(mockProduct);
    assertEquals(200, response.getStatusCodeValue());
    assertEquals("Product created with id: " + mockProduct.getId(), response.getBody());

  }

  @Test
  void testCreateProduct_TimeoutException() throws JsonProcessingException, TimeoutException {
    Product mockProduct = new Product (1, "Product 1", "Description 1", 10.0f, Date.valueOf("2023-4-12"));

    doThrow(new TimeoutException("Timeout Occurred")).when(service).createProduct(mockProduct);

    assertThrows(TimeoutException.class, ()-> service.createProduct(mockProduct));
  }

  @Test
  void testCreateProduct_JsonProcessingException() throws JsonProcessingException, TimeoutException {
    Product mockProduct = new Product (1, "Product 1", "Description 1", 10.0f, Date.valueOf("2023-4-12"));

    doThrow(new JsonProcessingException("JsonProcessingException: Error processing JSON"){}).when(service).createProduct(mockProduct);

    assertThrows(JsonProcessingException.class, ()-> service.createProduct(mockProduct));
  }

  @Test
  void testUpdateProduct() throws JsonProcessingException, TimeoutException {
    Product mockProduct = new Product (1, "Product 1", "Description 1", 10.0f, Date.valueOf("2023-4-12"));

    ResponseEntity<?> response = controller.updateProduct("1", mockProduct);
    assertEquals(200, response.getStatusCodeValue());
    assertEquals("Product updated with id: " + "1", response.getBody());
  }

  @Test
  void testUpdateProduct_TimeoutException() throws JsonProcessingException, TimeoutException {
    Product mockProduct = new Product (1, "Product 1", "Description 1", 10.0f, Date.valueOf("2023-4-12"));

    doThrow(new TimeoutException("TimeoutException: Timeout occurred")).when(service).updateProduct(mockProduct);

    assertThrows(TimeoutException.class, ()-> service.updateProduct(mockProduct));
  }

  @Test
  void testUpdateProduct_JsonProcessingException() throws JsonProcessingException, TimeoutException {
    Product mockProduct = new Product (1, "Product 1", "Description 1", 10.0f, Date.valueOf("2023-4-12"));

    doThrow(new JsonProcessingException("JsonProcessingException: Error processing JSON"){}).when(service).updateProduct(mockProduct);

    assertThrows(JsonProcessingException.class, ()-> service.updateProduct(mockProduct));
  }

  @Test
  void testDeleteProduct() throws TimeoutException, JsonProcessingException {

    int mockId = 1;

    ResponseEntity<?> response = controller.deleteProduct(mockId);
    assertEquals(200, response.getStatusCodeValue());
    assertEquals("Product deleted with id: " + mockId, response.getBody());

  }

  @Test
  void testDeleteProduct_TimeoutException() throws TimeoutException, JsonProcessingException {

    int mockId = 1;

    doThrow(new TimeoutException("TimeoutException: Timeout occurred")).when(service).deleteProduct(mockId);

    assertThrows(TimeoutException.class, ()-> service.deleteProduct(mockId));


  }
}
