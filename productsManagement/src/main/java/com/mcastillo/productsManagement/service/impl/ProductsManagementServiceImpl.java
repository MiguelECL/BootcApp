package com.mcastillo.productsManagement.service.impl;

import com.amazonaws.services.sqs.AmazonSQSRequester;
import com.amazonaws.services.sqs.AmazonSQSRequesterClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcastillo.Product;
import com.mcastillo.productsManagement.service.ProductsManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

///  This service sends messages through SQS to the products service.
@Service
public class ProductsManagementServiceImpl implements ProductsManagementService {

	@Value("${sqs.timeout}")
	private int TIMEOUT;

	@Value("${sqs.queue}")
	private final String queueURL;

	// AmazonSQSRequester is class that that allows for two-way communication with virtual queues
	// It creates a temporary queue for each response
	private final AmazonSQSRequester sqsRequester;
	ObjectMapper objectMapper = new ObjectMapper();

	Logger logger = LoggerFactory.getLogger(ProductsManagementServiceImpl.class);

	//no args constructor
	public ProductsManagementServiceImpl() {
		this.queueURL = System.getenv("QUEUE_URL");
		this.sqsRequester = AmazonSQSRequesterClientBuilder.defaultClient();
	}

	// args constructor for testing
	public ProductsManagementServiceImpl(String queueURL, AmazonSQSRequester sqsRequester) {
		this.queueURL = queueURL;
		this.sqsRequester = sqsRequester;
	}

	public String getProducts() {
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
			response = sqsRequester.sendMessageAndGetResponse(request, TIMEOUT, TimeUnit.SECONDS);
			return response.getBody();
		} catch (TimeoutException e){
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public String createProduct(Product product)  {
		Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
		messageAttributes.put("action", new MessageAttributeValue()
				.withDataType("String")
				.withStringValue("POST"));

		SendMessageRequest request = null;
		try {
			request = new SendMessageRequest()
					.withQueueUrl(queueURL)
					.withMessageAttributes(messageAttributes)
					.withMessageBody(objectMapper.writeValueAsString(product));
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}

		try{
			sqsRequester.sendMessageAndGetResponse(request, TIMEOUT, TimeUnit.SECONDS);
			return objectMapper.writeValueAsString(product);
		} catch (TimeoutException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (JsonProcessingException e){
			throw new IllegalArgumentException(e.getMessage(), e);
		}
	}

	public String updateProduct(Product product) {
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
			sqsRequester.sendMessageAndGetResponse(request, TIMEOUT, TimeUnit.SECONDS);
			return objectMapper.writeValueAsString(product);
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

		Message response;

		try{
			sqsRequester.sendMessageAndGetResponse(request, TIMEOUT, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			throw new RuntimeException(e.getMessage(), e);
		}

	}

}
