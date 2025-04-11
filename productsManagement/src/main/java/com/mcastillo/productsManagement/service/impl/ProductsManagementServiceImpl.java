package com.mcastillo.productsManagement.service.impl;

import com.amazonaws.services.sqs.AmazonSQSRequester;
import com.amazonaws.services.sqs.AmazonSQSRequesterClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcastillo.DatabaseException;
import com.mcastillo.Product;
import com.mcastillo.productsManagement.service.ProductsManagementService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class ProductsManagementServiceImpl implements ProductsManagementService {

	@Value("${sqs.timeout}")
	private int timeout;

	@Value("${sqs.queue}")
	private final String queueURL;

	private final AmazonSQSRequester sqsRequester;
	ObjectMapper objectMapper = new ObjectMapper();

	public ProductsManagementServiceImpl() {
		this.queueURL = System.getenv("QUEUE_URL");
		this.sqsRequester = AmazonSQSRequesterClientBuilder.defaultClient();
	}

	public List<Product> getProducts() {
		Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
		messageAttributes.put("action", new MessageAttributeValue()
				.withDataType("String")
				.withStringValue("GET"));

		SendMessageRequest request = new SendMessageRequest()
				.withQueueUrl(queueURL)
				.withMessageAttributes(messageAttributes)
				.withMessageBody("GET PRODUCTS");

		Message response;

		try {
			response = sqsRequester.sendMessageAndGetResponse(request, timeout, TimeUnit.SECONDS);
			String responseString = response.getBody();
			return objectMapper.readValue(responseString, new TypeReference<List<Product>>(){});
		} catch (TimeoutException e){
			throw new RuntimeException(e.getMessage(), e);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public Product createProduct(Product product)  {
		Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
		messageAttributes.put("action", new MessageAttributeValue()
				.withDataType("String")
				.withStringValue("POST"));

		SendMessageRequest request;

		try {
			request = new SendMessageRequest()
					.withQueueUrl(queueURL)
					.withMessageAttributes(messageAttributes)
					.withMessageBody(objectMapper.writeValueAsString(product));
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}

		try{
			Message message = sqsRequester.sendMessageAndGetResponse(request, timeout, TimeUnit.SECONDS);
			String response = message.getBody();
			return objectMapper.readValue(response, Product.class);
		} catch (TimeoutException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (JsonProcessingException e){
			throw new IllegalArgumentException(e.getMessage(), e);
		}
	}

	public Product updateProduct(Product product) {
		Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
		messageAttributes.put("action", new MessageAttributeValue()
				.withDataType("String")
				.withStringValue("PUT"));

		SendMessageRequest request = null;
		try {
			request = new SendMessageRequest()
					.withQueueUrl(queueURL)
					.withMessageAttributes(messageAttributes)
					.withMessageBody(objectMapper.writeValueAsString(product));
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException(e);
		}

		try{
			Message response = sqsRequester.sendMessageAndGetResponse(request, timeout, TimeUnit.SECONDS);
			if (response.getBody().equals("Failure to update from database")){
				throw new DatabaseException("Failure to update from database");
			} else {
				return objectMapper.readValue(response.getBody(), Product.class);
			}
		} catch (TimeoutException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (JsonProcessingException e){
			throw new IllegalArgumentException(e.getMessage(), e);
		}
	}

	public void deleteProduct(int id) {
		Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
		messageAttributes.put("action", new MessageAttributeValue()
				.withDataType("String")
				.withStringValue("DELETE"));

		SendMessageRequest request = new SendMessageRequest()
				.withQueueUrl(queueURL)
				.withMessageAttributes(messageAttributes)
				.withMessageBody(String.valueOf(id));

		try{
			Message response = sqsRequester.sendMessageAndGetResponse(request, timeout, TimeUnit.SECONDS);
			if (response.getBody().equals("Failure to delete from database"))
				throw new DatabaseException("Failure to delete from database");
		} catch (TimeoutException e) {
			throw new RuntimeException(e.getMessage(), e);
		}

	}

}
