package com.mcastillo.productsService.service.impl;

import com.amazonaws.services.sqs.*;
import com.amazonaws.services.sqs.model.Message;
import com.mcastillo.productsService.service.ProductsServiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.mcastillo.productsService.repository.ProductsServiceRepository;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ProductsServiceServiceImpl implements ProductsServiceService {

    private final Logger logger = LoggerFactory.getLogger(ProductsServiceServiceImpl.class);

    private final String queueURL;
    private final AmazonSQS sqsClient;
    private final AmazonSQSResponder sqsResponder;
    private final ProductsServiceRepository repository;
    private final ExecutorService executorService;

    public ProductsServiceServiceImpl(ProductsServiceRepository repository){
        this.repository = repository;
        this.sqsClient = AmazonSQSClientBuilder.defaultClient();
        this.sqsResponder = AmazonSQSResponderClientBuilder.defaultClient();
        this.queueURL = System.getenv("QUEUE_URL");
        this.executorService = Executors.newFixedThreadPool(10);
    }

    @PostConstruct
    @Override
    public void initializePolling(){
        new Thread(this::pollQueueContinuously).start();
    }

    protected void pollQueueContinuously() {
        while (!Thread.currentThread().isInterrupted()) {
            pollQueue();
        }
    }

    @Override
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

    @Override
    public void processMessage(Message message) {
        String response = repository.executeQuery(message);
        sqsResponder.sendResponseMessage(MessageContent.fromMessage(message),new MessageContent(response));
    }

    @Override
    public void deleteMessage(Message message) {
        DeleteMessageRequest deleteMessageRequest = new DeleteMessageRequest()
                .withQueueUrl(queueURL)
                .withReceiptHandle(message.getReceiptHandle());
        sqsClient.deleteMessage(deleteMessageRequest);
        logger.info("Deleted message: {}", message.getBody());
    }
}