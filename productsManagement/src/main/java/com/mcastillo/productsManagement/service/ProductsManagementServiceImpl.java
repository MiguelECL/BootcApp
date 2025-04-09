package com.mcastillo.productsManagement.service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.sqs.AmazonSQSRequester;
import com.amazonaws.services.sqs.AmazonSQSRequesterClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcastillo.Product;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

///  This service sends messages through SQS to the products service.
@Service
public class ProductsManagementServiceImpl implements ProductsManagementService {

	@Value("${sqs.timeout:5}")
	private int TIMEOUT;

	// The queue URL is set in the environment variable QUEUE_URL
	String queueURL;

	// AmazonSQSRequester is class that that allows for two-way communication with virtual queues
	// It creates a temporary queue for each response
	private final AmazonSQSRequester sqsRequester;
	ObjectMapper objectMapper = new ObjectMapper();

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

	public String getProducts() throws TimeoutException {
		Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
		messageAttributes.put("action", new MessageAttributeValue()
				.withDataType("String")
				.withStringValue("GET"));

		SendMessageRequest request = new SendMessageRequest()
				.withQueueUrl(queueURL)
				.withMessageAttributes(messageAttributes)
				.withMessageBody("GET PRODUCTS");

		// slf4j logger
		logger.info("Getting products");
		Message response;

		try {
			response = sqsRequester.sendMessageAndGetResponse(request, TIMEOUT, TimeUnit.SECONDS);
			return response.getBody();
		} catch (AmazonClientException e){
			logger.error("Timeout while waiting for SQS Response", e);
			throw new TimeoutException(e.getMessage());
		}
	}

	public String createProduct(Product product) throws TimeoutException, JsonProcessingException {
		Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
		messageAttributes.put("action", new MessageAttributeValue()
				.withDataType("String")
				.withStringValue("POST"));

		SendMessageRequest request = new SendMessageRequest()
				.withQueueUrl(queueURL)
				.withMessageAttributes(messageAttributes)
				.withMessageBody(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(product));

		try{
			sqsRequester.sendMessageAndGetResponse(request, TIMEOUT, TimeUnit.SECONDS);
			return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(product);
		} catch (AmazonClientException e) {
			logger.error("Timeout while waiting for SQS Response", e);
			throw new TimeoutException(e.getMessage());
		}
	}

	public String updateProduct(Product product) throws TimeoutException, JsonProcessingException {
		Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
		messageAttributes.put("action", new MessageAttributeValue()
				.withDataType("String")
				.withStringValue("PUT"));

		SendMessageRequest request = new SendMessageRequest()
				.withQueueUrl(queueURL)
				.withMessageAttributes(messageAttributes)
				.withMessageBody(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(product));

		try{
			sqsRequester.sendMessageAndGetResponse(request, TIMEOUT, TimeUnit.SECONDS);
			return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(product);
		} catch (AmazonClientException e) {
			logger.error("Timeout while waiting for SQS Response", e);
			throw new TimeoutException(e.getMessage());
		}
	}

	public void deleteProduct(int id) throws TimeoutException{
		Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
		messageAttributes.put("action", new MessageAttributeValue()
				.withDataType("String")
				.withStringValue("DELETE"));

		SendMessageRequest request = new SendMessageRequest()
				.withQueueUrl(queueURL)
				.withMessageAttributes(messageAttributes)
				.withMessageBody(String.valueOf(id));

		logger.info("Requesting Deletion Response");
		Message response;

		try{
			response = sqsRequester.sendMessageAndGetResponse(request, TIMEOUT, TimeUnit.SECONDS);
			logger.info("Response: {}", response.getBody());
		} catch (AmazonClientException e) {
			logger.error("Timeout while waiting for SQS Response", e);
			throw new TimeoutException(e.getMessage());
		}

	}

}
