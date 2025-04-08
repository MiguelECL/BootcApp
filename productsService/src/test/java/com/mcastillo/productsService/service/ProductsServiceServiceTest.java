package com.mcastillo.productsService.service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSResponder;
import com.amazonaws.services.sqs.MessageContent;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
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

	@InjectMocks
	private ProductsServiceService productsServiceService;

	private CountDownLatch latch;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		String queueUrl = "mockQueueURL";
		productsServiceService = new ProductsServiceService(repository, sqsClient, sqsResponder, queueUrl);
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
		// Prepare a mock list of messages
		Message mockMessage = new Message();
		mockMessage.setBody("Test message body");
		when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
				.thenReturn(new ReceiveMessageResult().withMessages(mockMessage));

		// Call pollQueue
		productsServiceService.pollQueue();

		// Verify that the SQS client's receiveMessage was called once
		verify(sqsClient, times(1)).receiveMessage(any(ReceiveMessageRequest.class));
	}

	@Test
	void test_initializePolling() throws InterruptedException {
		// Create a spy of the ProductsServiceService to spy on the method calls
		ProductsServiceService spyService = spy(productsServiceService);

		// Mock the pollQueueContinuously method to complete quickly
		doAnswer(invocation -> {
			latch.countDown();  // Signal that the polling method has been called
			return null;
		}).when(spyService).pollQueueContinuously();

		// Call the initializePolling method, which starts the thread
		spyService.initializePolling();

		// Wait for the polling thread to start and execute the mocked pollQueueContinuously
		latch.await();

		// Verify that the pollQueueContinuously method was called at least once
		verify(spyService, times(1)).pollQueueContinuously();
	}

	@Test
	void test_PollQueueContinuously() {
		// Create a spy of the ProductsServiceService to mock the pollQueue() method
		ProductsServiceService spyService = spy(productsServiceService);

		// Mock pollQueue() to return immediately without executing its logic
		doThrow(new RuntimeException("Mocked pollQueue")).when(spyService).pollQueue();

		// Use assertThrows to verify that a RuntimeException is thrown when pollQueueContinuously is called
		RuntimeException thrown = assertThrows(RuntimeException.class, spyService::pollQueueContinuously);

		// Verify that the exception message matches the expected message
		assertEquals("Mocked pollQueue", thrown.getMessage());

		// Verify that pollQueue was called at least once
		verify(spyService, times(1)).pollQueue();

	}

}