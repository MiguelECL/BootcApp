package com.mcastillo.productsManagement.service;

import com.amazonaws.services.sqs.AmazonSQSRequester;
import com.amazonaws.services.sqs.AmazonSQSRequesterClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

///  This service sends messages through SQS to the products service.
@Service
public class ProductsManagementService {

  // The queue URL is set in the environment variable QUEUE_URL
  String queueURL = System.getenv("QUEUE_URL");

  // AmazonSQSRequester is class that that allows for two-way communication with virtual queues
  // It creates a temporary queue for each response
  private final AmazonSQSRequester sqsRequester = AmazonSQSRequesterClientBuilder.defaultClient();

  public String getProducts() throws TimeoutException {
    Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
    messageAttributes.put("action", new MessageAttributeValue()
        .withDataType("String")
        .withStringValue("GET"));

    SendMessageRequest request = new SendMessageRequest()
      .withQueueUrl(queueURL)
      .withMessageAttributes(messageAttributes)
      .withMessageBody("Test");

    //  - creates a temporary queue
    //  - attaches its URL as an attribute on the message
    //  - sends the message
    //  - receives the response from the temporary queue
    //  - deletes the temporary queue
    //  - returns the response

    System.out.println("requesting response");
    Message response;
    response = sqsRequester.sendMessageAndGetResponse(request, 5, TimeUnit.SECONDS);
    return response.getBody();
  }

  public void createProduct(){
  }

  public void updateProduct(){
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

    //  - creates a temporary queue
    //  - attaches its URL as an attribute on the message
    //  - sends the message
    //  - receives the response from the temporary queue
    //  - deletes the temporary queue
    //  - returns the response

    System.out.println("requesting deletion response");
    Message response;
    response = sqsRequester.sendMessageAndGetResponse(request, 5, TimeUnit.SECONDS);
    System.out.println(response.getBody());
  }

}
