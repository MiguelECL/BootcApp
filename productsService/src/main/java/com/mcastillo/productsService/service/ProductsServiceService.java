package com.mcastillo.productsService.service;

import com.amazonaws.services.sqs.*;
import com.amazonaws.services.sqs.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final Logger logger = LoggerFactory.getLogger(ProductsServiceService.class);

    private final String queueURL;
    private final AmazonSQS sqsClient;
    private final AmazonSQSResponder sqsResponder;
    private final ProductsServiceRepository repository;
    private final ExecutorService executorService;

    @Autowired
    public ProductsServiceService(ProductsServiceRepository repository){
        this.repository = repository;
        this.sqsClient = AmazonSQSClientBuilder.defaultClient();
        this.sqsResponder = AmazonSQSResponderClientBuilder.defaultClient();
        this.queueURL = System.getenv("QUEUE_URL");
        this.executorService = Executors.newFixedThreadPool(10);
    }

    // Constructor for testing
    public ProductsServiceService(ProductsServiceRepository repository,
                                  AmazonSQS sqsClient,
                                  AmazonSQSResponder sqsResponder,
                                  String queueURL,
                                  ExecutorService executorService){

        this.repository = repository;
        this.sqsClient = sqsClient;
        this.sqsResponder = sqsResponder;
        this.queueURL = queueURL;
        this.executorService = executorService;
    }

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
        logger.info("Polling SQS...");

        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest()
                .withQueueUrl(queueURL)
                .withMessageAttributeNames("All")
                .withMaxNumberOfMessages(5)
                .withWaitTimeSeconds(20);

        List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).getMessages();
        logger.info("Received {} messages", messages.size());
        logger.info(messages.toString());

        for (Message message : messages){
            executorService.submit(() -> {
                try {
                    processMessage(message);
                    deleteMessage(message);
                } catch (Exception e) {
                    logger.trace("Error processing message:", e);
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
        logger.info("Deleted message: {}", message.getBody());
    }
}