package com.mcastillo.productsService.service;

import com.amazonaws.services.sqs.*;
import com.amazonaws.services.sqs.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.mcastillo.productsService.repository.ProductsServiceRepository;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/// This service receives messages from products Management through SQS and communicates with the database.
@Service
public class ProductsServiceService {

    private final String queueURL;
    private final AmazonSQS sqsClient;
    private final AmazonSQSResponder sqsResponder;
    private final ProductsServiceRepository repository;

    @Autowired
    public ProductsServiceService(ProductsServiceRepository repository){
        this.repository = repository;
        this.sqsClient = AmazonSQSClientBuilder.defaultClient();
        this.sqsResponder = AmazonSQSResponderClientBuilder.defaultClient();
        this.queueURL = System.getenv("QUEUE_URL");
    }

    // Constructor for testing
    public ProductsServiceService(ProductsServiceRepository repository,
                                  AmazonSQS sqsClient,
                                  AmazonSQSResponder sqsResponder,
                                  String queueURL) {
        this.repository = repository;
        this.sqsClient = sqsClient;
        this.sqsResponder = sqsResponder;
        this.queueURL = queueURL;
    }

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @PostConstruct
    public void initializePolling(){
        new Thread(this::pollQueueContinuously).start();
    }

    protected void pollQueueContinuously() {
        while (true) {
            pollQueue();
        }
    }

    public void pollQueue() {
        System.out.println("Polling SQS...");

        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest()
                .withQueueUrl(queueURL)
                .withMessageAttributeNames("All")
                .withMaxNumberOfMessages(5)
                .withWaitTimeSeconds(20);

        List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).getMessages();
        System.out.println(messages);


        for (Message message : messages){
            executorService.submit(() -> {
                try {
                    System.out.println("Received message: " + message.getBody());
                    processMessage(message);
                    deleteMessage(message);
                } catch (Exception e) {
                    System.out.println("Error processing message: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }
    }

    public void processMessage(Message message) {
        String response = repository.executeQuery(message);
        sqsResponder.sendResponseMessage(MessageContent.fromMessage(message),new MessageContent(response));
    }

    public void deleteMessage(Message message) {
        DeleteMessageRequest deleteMessageRequest = new DeleteMessageRequest()
                .withQueueUrl(queueURL)
                .withReceiptHandle(message.getReceiptHandle());
        sqsClient.deleteMessage(deleteMessageRequest);
        System.out.println("Deleted message: " + message.getBody());
    }
}