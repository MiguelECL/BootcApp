package com.mcastillo.productsManagement.service;

import com.amazonaws.services.sqs.model.SendMessageResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.stereotype.Service;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;

import java.sql.SQLOutput;

///  This service sends messages through SQS to the products service.
@Service
public class ProductsManagementService {

  String queueURL = System.getenv("QUEUE_URL");
  final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();

  public void getProducts(){
    SendMessageRequest msgRequest = new SendMessageRequest()
      .withQueueUrl(queueURL)
      .withMessageBody("Test")
      .withDelaySeconds(2);

    SendMessageResult msgResult = sqs.sendMessage(msgRequest);
    msgResult.getMessageId();
  }

  public void createProduct(){
    SendMessageRequest msgRequest = new SendMessageRequest()
      .withQueueUrl(queueURL)
      .withMessageBody("Test")
      .withDelaySeconds(2);

    SendMessageResult msgResult = sqs.sendMessage(msgRequest);
    msgResult.getMessageId();
  }

  public void updateProduct(){
    SendMessageRequest msgRequest = new SendMessageRequest()
      .withQueueUrl(queueURL)
      .withMessageBody("Test")
      .withDelaySeconds(2);

    SendMessageResult msgResult = sqs.sendMessage(msgRequest);
    msgResult.getMessageId();
  }

  public void deleteProduct(){
    SendMessageRequest msgRequest = new SendMessageRequest()
      .withQueueUrl(queueURL)
      .withMessageBody("Test")
      .withDelaySeconds(2);

    SendMessageResult msgResult = sqs.sendMessage(msgRequest);
    msgResult.getMessageId();
  }

}
