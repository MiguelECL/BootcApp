package com.mcastillo.productsManagement.service.impl;

import com.amazonaws.services.sqs.AmazonSQSRequester;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcastillo.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class ProductsManagementServiceImplTest {

  private static final String queueURL = "test_queue_url";

  @Mock
  AmazonSQSRequester mockRequester;

  @Mock
  ObjectMapper objectMapper;

  @InjectMocks
  private ProductsManagementServiceImpl service;

  @Value("${sqs.timeout}")
  private int TIMEOUT;

  @Test
  void test_getProducts() throws TimeoutException {

    Map<String, MessageAttributeValue> mockMessageAttributes = new HashMap<>();
    mockMessageAttributes.put("action", new MessageAttributeValue()
      .withDataType("String")
      .withStringValue("GET"));

   SendMessageRequest mockRequest = new SendMessageRequest()
      .withMessageAttributes(mockMessageAttributes)
      .withMessageBody("GET PRODUCTS");

    Message mockResponse = new Message().withBody("Response");
    when(mockRequester.sendMessageAndGetResponse(mockRequest, 0, TimeUnit.SECONDS)).thenReturn(mockResponse);

    service.getProducts();
    assertEquals("Response", mockResponse.getBody());
  }

  @Test
  void test_getProducts_Exception() throws TimeoutException {

    Map<String, MessageAttributeValue> mockMessageAttributes = new HashMap<>();
    mockMessageAttributes.put("action", new MessageAttributeValue()
            .withDataType("String")
            .withStringValue("GET"));

    SendMessageRequest mockRequest = new SendMessageRequest()
            .withQueueUrl(queueURL)
            .withMessageAttributes(mockMessageAttributes)
            .withMessageBody("GET PRODUCTS");

    when(mockRequester.sendMessageAndGetResponse(mockRequest, 0, TimeUnit.SECONDS)).thenThrow(new TimeoutException("Error"));

    assertThrows(RuntimeException.class, ()-> service.getProducts());
  }

  @Test
  void test_createProduct() throws JsonProcessingException, TimeoutException {
    Product product = new Product(1, "test_product", "test_description", 10.0f, Date.valueOf("2023-10-01"));
    String productJson = "{\"id\":1,\"name\":\"test_product\",\"description\":\"test_description\",\"price\":10.0,\"createDate\":\"2023-10-01\"}";

    when(objectMapper.writeValueAsString(product)).thenReturn(productJson);
    when(objectMapper.readValue("Response", Product.class)).thenReturn(product);

    Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
    messageAttributes.put("action", new MessageAttributeValue()
      .withDataType("String")
      .withStringValue("POST"));

    SendMessageRequest mockRequest = new SendMessageRequest()
      .withMessageAttributes(messageAttributes)
      .withMessageBody(objectMapper.writeValueAsString(product));

    Message mockResponse = new Message().withBody("Response");

    when(mockRequester.sendMessageAndGetResponse(mockRequest,0,TimeUnit.SECONDS)).thenReturn(mockResponse);

    Product response = service.createProduct(product);

    assertEquals(product, response);

  }

  @Test
  void test_createProduct_Exception() throws JsonProcessingException, TimeoutException {
    Product product = new Product(1, "test_product", "test_description", 10.0f, Date.valueOf("2023-10-01"));

    Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
    messageAttributes.put("action", new MessageAttributeValue()
            .withDataType("String")
            .withStringValue("POST"));

    SendMessageRequest mockRequest = new SendMessageRequest()
            .withQueueUrl(queueURL)
            .withMessageAttributes(messageAttributes)
            .withMessageBody(objectMapper.writeValueAsString(product));

    Message mockResponse = new Message().withBody("Response");

    assertThrows(RuntimeException.class, ()-> service.createProduct(product));

  }

  @Test
  void test_updateProduct() throws JsonProcessingException, TimeoutException {
    Product product = new Product(1, "test_product", "test_description", 10.0f, Date.valueOf("2023-10-01"));
    String productJson = "{\"id\":1,\"name\":\"test_product\",\"description\":\"test_description\",\"price\":10.0,\"createDate\":\"2023-10-01\"}";

    when(objectMapper.writeValueAsString(product)).thenReturn(productJson);
    when(objectMapper.readValue("Response", Product.class)).thenReturn(product);

    Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
    messageAttributes.put("action", new MessageAttributeValue()
      .withDataType("String")
      .withStringValue("PUT"));

    SendMessageRequest mockRequest = new SendMessageRequest()
      .withMessageAttributes(messageAttributes)
      .withMessageBody(objectMapper.writeValueAsString(product));

    Message mockResponse = new Message().withBody("Response");
    when(mockRequester.sendMessageAndGetResponse(mockRequest, 0, TimeUnit.SECONDS)).thenReturn(mockResponse);

    Product response = service.updateProduct(product);

    assertEquals(product, response);
  }

  @Test
  void test_updateProductException() throws JsonProcessingException, TimeoutException {
    Product product = new Product(1, "test_product", "test_description", 10.0f, Date.valueOf("2023-10-01"));

    Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
    messageAttributes.put("action", new MessageAttributeValue()
            .withDataType("String")
            .withStringValue("PUT"));

    SendMessageRequest mockRequest = new SendMessageRequest()
            .withQueueUrl(queueURL)
            .withMessageAttributes(messageAttributes)
            .withMessageBody(objectMapper.writeValueAsString(product));

    when(mockRequester.sendMessageAndGetResponse(mockRequest, 0, TimeUnit.SECONDS)).thenThrow(new TimeoutException("Error"));

    assertThrows(RuntimeException.class, ()->service.updateProduct(product));

  }

  @Test
  void test_deleteProduct() throws TimeoutException, JsonProcessingException {
    int id = 1;
    Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
    messageAttributes.put("action", new MessageAttributeValue()
      .withDataType("String")
      .withStringValue("DELETE"));

    SendMessageRequest request = new SendMessageRequest()
      .withMessageAttributes(messageAttributes)
      .withMessageBody(String.valueOf(id));

    when(mockRequester.sendMessageAndGetResponse(request,0, TimeUnit.SECONDS)).thenReturn(new Message().withBody("Response"));
    Message mockResponse = new Message().withBody("Response");
    service.deleteProduct(id);

    assertEquals("Response",mockResponse.getBody());
  }

  @Test
  void test_deleteProduct_Exception() throws TimeoutException {
    int id = 1;
    Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
    messageAttributes.put("action", new MessageAttributeValue()
            .withDataType("String")
            .withStringValue("DELETE"));

    SendMessageRequest request = new SendMessageRequest()
            .withQueueUrl(queueURL)
            .withMessageAttributes(messageAttributes)
            .withMessageBody(String.valueOf(id));

    when(mockRequester.sendMessageAndGetResponse(request,0, TimeUnit.SECONDS)).thenThrow(new TimeoutException("Error"));

    assertThrows(RuntimeException.class, ()-> service.deleteProduct(id));

  }
}
