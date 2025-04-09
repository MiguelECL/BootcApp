package com.mcastillo.productsService.service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSResponder;
import com.amazonaws.services.sqs.MessageContent;
import com.amazonaws.services.sqs.model.*;
import com.mcastillo.productsService.repository.ProductsServiceRepository;
import com.mcastillo.productsService.service.ProductsServiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class ProductsServiceServiceTest {

	@Mock
	private AmazonSQS sqsClient;

	@Mock
	private AmazonSQSResponder sqsResponder;

	@Mock
	private ProductsServiceRepository repository;

	@Mock
	private ExecutorService executorService;

	@InjectMocks
	private ProductsServiceService productsServiceService;

	private CountDownLatch latch;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		String queueUrl = "mockQueueURL";
		productsServiceService = new ProductsServiceService(repository, sqsClient, sqsResponder, queueUrl, executorService);
		latch = new CountDownLatch(1);
	}

	@Test
	void test_ProcessMessage() {
		Message mockMessage = new Message();
		mockMessage.setBody("Test message body");
		when(repository.executeQuery(mockMessage)).thenReturn("Response message");

		productsServiceService.processMessage(mockMessage);

		ArgumentCaptor<MessageContent> messageCaptor = ArgumentCaptor.forClass(MessageContent.class);
		verify(sqsResponder).sendResponseMessage(any(), messageCaptor.capture());

		MessageContent capturedResponse = messageCaptor.getValue();
		assert capturedResponse.getMessageBody().equals("Response message");
	}

	@Test
	void test_DeleteMessage() {
		Message mockMessage = new Message();
		mockMessage.setReceiptHandle("mockReceiptHandle");
		mockMessage.setBody("Test message");

		productsServiceService.deleteMessage(mockMessage);

		verify(sqsClient).deleteMessage(any());
	}

	@Test
	void testPollQueue_receivesMessages() {
		Message mockMessage = new Message();
		mockMessage.setBody("Test message body");
		when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
				.thenReturn(new ReceiveMessageResult().withMessages(mockMessage));

		productsServiceService.pollQueue();

		verify(sqsClient, times(1)).receiveMessage(any(ReceiveMessageRequest.class));
	}

	@Test
	void testPollQueue_deletesMessages() throws InterruptedException {
		// Prepare mock message
		Message mockMessage = new Message();
		mockMessage.setBody("Test message body");
		mockMessage.setReceiptHandle("MockReceiptHandle");

		// Mock the receiveMessage response
		ReceiveMessageResult receiveMessageResult = new ReceiveMessageResult().withMessages(mockMessage);

		// Mock the deleteMessage response
		DeleteMessageResult deleteMessageResult = new DeleteMessageResult();

		// Mock the behavior of sqsClient
		when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
				.thenReturn(receiveMessageResult);
		when(sqsClient.deleteMessage(any(DeleteMessageRequest.class)))
				.thenReturn(deleteMessageResult);

		// Create CountDownLatch to synchronize thread completion
		CountDownLatch latch = new CountDownLatch(1);

		// Mock the executor service to simulate message processing
		doAnswer(invocation -> {
			Runnable task = invocation.getArgument(0);
			new Thread(() -> {
				task.run();  // Run the task in the current thread to simplify the test
				latch.countDown();  // Count down when the task is finished
			}).start();
			return null;
		}).when(executorService).submit(any(Runnable.class));

		// Instantiate ProductsServiceService with the mocked dependencies
		productsServiceService = new ProductsServiceService(repository, sqsClient, sqsResponder, "mockQueueURL", executorService);

		// Call the method under test
		productsServiceService.pollQueue();

		// Wait for the executor service to finish processing the message
		latch.await();  // Wait for the task to complete

		// Verify deleteMessage is called once
		verify(sqsClient, times(1)).deleteMessage(any(DeleteMessageRequest.class));
	}

	@Test
	void test_initializePolling() throws InterruptedException {
		ProductsServiceService spyService = spy(productsServiceService);

		doAnswer(invocation -> {
			latch.countDown();
			return null;
		}).when(spyService).pollQueueContinuously();

		spyService.initializePolling();

		latch.await();

		verify(spyService, times(1)).pollQueueContinuously();
	}

	@Test
	void test_PollQueueContinuously() {
		ProductsServiceService spyService = spy(productsServiceService);

		doThrow(new RuntimeException("Mocked pollQueue")).when(spyService).pollQueue();

		RuntimeException thrown = assertThrows(RuntimeException.class, spyService::pollQueueContinuously);

		assertEquals("Mocked pollQueue", thrown.getMessage());

		verify(spyService, times(1)).pollQueue();

	}

}