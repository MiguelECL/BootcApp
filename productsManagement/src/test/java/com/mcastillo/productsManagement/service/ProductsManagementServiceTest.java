package com.mcastillo.productsManagement.service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.sqs.AmazonSQSRequester;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcastillo.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class ProductsManagementServiceTest {

  private static final String queueURL = "test_queue_url";

  @Mock
  AmazonSQSRequester mockRequester;

  private ProductsManagementService service;

  ObjectMapper objectMapper;

  @Value("${sqs.timeout}")
  private int TIMEOUT;

  @BeforeEach
  void setUp(){
    // Initialize the service with the mocked AmazonSQSRequester
    service = new ProductsManagementServiceImpl(queueURL, mockRequester);
    objectMapper = new ObjectMapper();
  }

  @Test
  void test_getProducts() throws TimeoutException {

    Map<String, MessageAttributeValue> mockMessageAttributes = new HashMap<>();
    mockMessageAttributes.put("action", new MessageAttributeValue()
      .withDataType("String")
      .withStringValue("GET"));

   SendMessageRequest mockRequest = new SendMessageRequest()
      .withQueueUrl(queueURL)
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

    Message mockResponse = new Message().withBody("Response");
    when(mockRequester.sendMessageAndGetResponse(mockRequest, 0, TimeUnit.SECONDS)).thenThrow(new AmazonClientException("Error"));

    assertThrows(TimeoutException.class, ()-> service.getProducts());
  }

  @Test
  void test_createProduct() throws JsonProcessingException, TimeoutException {
    Product product = new Product(1, "test_product", "test_description", 10.0f, Date.valueOf("2023-10-01"));

    Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
    messageAttributes.put("action", new MessageAttributeValue()
      .withDataType("String")
      .withStringValue("POST"));

    SendMessageRequest mockRequest = new SendMessageRequest()
      .withQueueUrl(queueURL)
      .withMessageAttributes(messageAttributes)
      .withMessageBody(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(product));

    Message mockResponse = new Message().withBody("Response");
    when(mockRequester.sendMessageAndGetResponse(mockRequest,0,TimeUnit.SECONDS)).thenReturn(mockResponse);

    service.createProduct(product);

    assertEquals("Response", mockResponse.getBody());

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
            .withMessageBody(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(product));

    Message mockResponse = new Message().withBody("Response");
    when(mockRequester.sendMessageAndGetResponse(mockRequest,0,TimeUnit.SECONDS)).thenThrow(new AmazonClientException("Error"));

    assertThrows(TimeoutException.class, ()-> service.createProduct(product));

  }

  @Test
  void test_updateProduct() throws JsonProcessingException, TimeoutException {
    Product product = new Product(1, "test_product", "test_description", 10.0f, Date.valueOf("2023-10-01"));

    Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
    messageAttributes.put("action", new MessageAttributeValue()
      .withDataType("String")
      .withStringValue("PUT"));

    SendMessageRequest mockRequest = new SendMessageRequest()
      .withQueueUrl(queueURL)
      .withMessageAttributes(messageAttributes)
      .withMessageBody(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(product));

    Message mockResponse = new Message().withBody("Response");
    when(mockRequester.sendMessageAndGetResponse(mockRequest, 0, TimeUnit.SECONDS)).thenReturn(mockResponse);

    service.updateProduct(product);

    assertEquals("Response", mockResponse.getBody());
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
            .withMessageBody(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(product));

    when(mockRequester.sendMessageAndGetResponse(mockRequest, 0, TimeUnit.SECONDS)).thenThrow(new AmazonClientException("Error"));

    assertThrows(TimeoutException.class, ()->service.updateProduct(product));

  }

  @Test
  void test_deleteProduct() throws TimeoutException, JsonProcessingException {
    int id = 1;
    Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
    messageAttributes.put("action", new MessageAttributeValue()
      .withDataType("String")
      .withStringValue("DELETE"));

    SendMessageRequest request = new SendMessageRequest()
      .withQueueUrl(queueURL)
      .withMessageAttributes(messageAttributes)
      .withMessageBody(String.valueOf(id));

    when(mockRequester.sendMessageAndGetResponse(request,0, TimeUnit.SECONDS)).thenReturn(new Message().withBody("Response"));
    Message mockResponse = new Message().withBody("Response");
    service.deleteProduct(id);

    assertEquals("Response",mockResponse.getBody());
  }

  @Test
  void test_deleteProduct_Exception() throws TimeoutException, JsonProcessingException {
    int id = 1;
    Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
    messageAttributes.put("action", new MessageAttributeValue()
            .withDataType("String")
            .withStringValue("DELETE"));

    SendMessageRequest request = new SendMessageRequest()
            .withQueueUrl(queueURL)
            .withMessageAttributes(messageAttributes)
            .withMessageBody(String.valueOf(id));

    when(mockRequester.sendMessageAndGetResponse(request,0, TimeUnit.SECONDS)).thenThrow(new AmazonClientException("Error"));

    assertThrows(TimeoutException.class, ()-> service.deleteProduct(id));

  }
}
