package com.mcastillo.productsService.service;

import com.amazonaws.services.sqs.*;
import com.amazonaws.services.sqs.model.Message;
import com.mcastillo.productsService.configuration.Queries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.mcastillo.productsService.repository.ProductsServiceRepository;

import javax.annotation.PostConstruct;
import java.util.List;

/// This service receives messages from products Management through SQS and communicates with the database.
@Service
public class ProductsServiceService {

    private final String queueURL = System.getenv("QUEUE_URL");
    private final AmazonSQS sqsClient = AmazonSQSClientBuilder.defaultClient();
    private final AmazonSQSResponder sqsResponder = AmazonSQSResponderClientBuilder.defaultClient();

    // Inject repository service
    @Autowired
    private ProductsServiceRepository repository;

    @Autowired
    private Queries queries;

    // Continuously poll for messages from the queue
    @PostConstruct
    public void pollQueue() {
        System.out.println("Polling SQS...");

        new Thread(() -> {
            while (true) {
                ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest()
                        .withQueueUrl(queueURL)
                        .withMessageAttributeNames("All")
                        .withMaxNumberOfMessages(5)
                        .withWaitTimeSeconds(5);

                List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).getMessages();
                System.out.println(messages);

                for (Message message : messages){
                    System.out.println("Received message: " + message.getBody());
                    processMessage(message);
                    deleteMessage(message);
                }
            }
        }).start();
    }

    public void processMessage(Message message) {
        // Process the message
        String action = message.getMessageAttributes().get("action").getStringValue();
        System.out.println(action);
        repository.executeQuery(action);
        String response = "Response";
        sqsResponder.sendResponseMessage(MessageContent.fromMessage(message),new MessageContent(response));
    }

    private void deleteMessage(Message message) {
        DeleteMessageRequest deleteMessageRequest = new DeleteMessageRequest()
                .withQueueUrl(queueURL)
                .withReceiptHandle(message.getReceiptHandle());
        sqsClient.deleteMessage(deleteMessageRequest);
        System.out.println("Deleted message: " + message.getBody());
    }
}