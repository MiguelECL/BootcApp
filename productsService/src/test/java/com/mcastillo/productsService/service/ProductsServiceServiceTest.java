package com.mcastillo.productsService.service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSResponder;
import com.amazonaws.services.sqs.MessageContent;
import com.amazonaws.services.sqs.model.*;
import com.mcastillo.productsService.repository.ProductsServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductsServiceServiceTest {

	@Mock
	private AmazonSQS sqsClient;

	@Mock
	private AmazonSQSResponder sqsResponder;

	@Mock
	private ProductsServiceRepository repository;

	@Mock
	private ExecutorService executorService;

	private ProductsServiceService serviceUnderTest;
	private final String MOCK_QUEUE_URL = "https://sqs.example.com/12345/test-queue";

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		serviceUnderTest = new ProductsServiceService(repository);

		ReflectionTestUtils.setField(serviceUnderTest, "sqsClient", sqsClient);
		ReflectionTestUtils.setField(serviceUnderTest, "sqsResponder", sqsResponder);
		ReflectionTestUtils.setField(serviceUnderTest, "queueURL", MOCK_QUEUE_URL);
		ReflectionTestUtils.setField(serviceUnderTest, "executorService", executorService);
	}

	@Test
	void test_processMessage_ShouldExecuteQueryAndSendResponse() {
		Message mockMessage = new Message()
				.withMessageId("test-message-id")
				.withBody("test message body");

		String expectedResponse = "response data";
		when(repository.executeQuery(mockMessage)).thenReturn(expectedResponse);

		serviceUnderTest.processMessage(mockMessage);

		verify(repository, times(1)).executeQuery(mockMessage);

		ArgumentCaptor<MessageContent> requestCaptor = ArgumentCaptor.forClass(MessageContent.class);
		ArgumentCaptor<MessageContent> responseCaptor = ArgumentCaptor.forClass(MessageContent.class);

		verify(sqsResponder, times(1)).sendResponseMessage(requestCaptor.capture(), responseCaptor.capture());

		assertEquals(expectedResponse, responseCaptor.getValue().getMessageBody());
	}

	@Test
	void deleteMessage_ShouldCallSqsClientWithCorrectParameters() {
		String receiptHandle = "test-receipt-handle";
		Message mockMessage = new Message()
				.withMessageId("test-message-id")
				.withReceiptHandle(receiptHandle)
				.withBody("test message body");

		serviceUnderTest.deleteMessage(mockMessage);

		ArgumentCaptor<DeleteMessageRequest> requestCaptor = ArgumentCaptor.forClass(DeleteMessageRequest.class);
		verify(sqsClient, times(1)).deleteMessage(requestCaptor.capture());

		DeleteMessageRequest capturedRequest = requestCaptor.getValue();
		assertEquals(MOCK_QUEUE_URL, capturedRequest.getQueueUrl());
		assertEquals(receiptHandle, capturedRequest.getReceiptHandle());
	}

	@Test
	void pollQueue_WithNoMessages_ShouldNotProcessAnything() {
		ReceiveMessageResult emptyResult = new ReceiveMessageResult().withMessages(Collections.emptyList());
		when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(emptyResult);

		serviceUnderTest.pollQueue();

		verify(sqsClient, times(1)).receiveMessage(any(ReceiveMessageRequest.class));
		verify(executorService, never()).submit(any(Runnable.class));
	}

	@Test
	void pollQueue_WithMessages_ShouldSubmitTasksToExecutorService() {
		Message message1 = new Message().withMessageId("id-1").withBody("body-1").withReceiptHandle("receipt-1");
		Message message2 = new Message().withMessageId("id-2").withBody("body-2").withReceiptHandle("receipt-2");
		List<Message> messages = Arrays.asList(message1, message2);

		ReceiveMessageResult mockResult = new ReceiveMessageResult().withMessages(messages);
		when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(mockResult);

		when(repository.executeQuery(any(Message.class))).thenReturn("test-response");

		List<Runnable> capturedRunnables = new ArrayList<>();
		doAnswer(invocation -> {
			Runnable runnable = invocation.getArgument(0);
			capturedRunnables.add(runnable);
			return null;
		}).when(executorService).submit(any(Runnable.class));

		serviceUnderTest.pollQueue();

		verify(sqsClient, times(1)).receiveMessage(any(ReceiveMessageRequest.class));
		verify(executorService, times(2)).submit(any(Runnable.class));

		for (Runnable runnable : capturedRunnables) {
			runnable.run();
		}

		verify(repository, times(1)).executeQuery(eq(message1));
		verify(repository, times(1)).executeQuery(eq(message2));
		verify(sqsClient, times(2)).deleteMessage(any(DeleteMessageRequest.class));
	}

	@Test
	void pollQueue_WithExceptionDuringProcessing_ShouldContinueWithNextMessage() {
		Message message1 = new Message().withMessageId("id-1").withBody("body-1");
		Message message2 = new Message().withMessageId("id-2").withBody("body-2");
		List<Message> messages = Arrays.asList(message1, message2);

		ReceiveMessageResult mockResult = new ReceiveMessageResult().withMessages(messages);
		when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(mockResult);

		when(repository.executeQuery(eq(message1))).thenThrow(new RuntimeException("Test exception"));
		when(repository.executeQuery(eq(message2))).thenReturn("response-2");

		doAnswer(invocation -> {
			Runnable runnable = invocation.getArgument(0);
			runnable.run();
			return null;
		}).when(executorService).submit(any(Runnable.class));

		serviceUnderTest.pollQueue();

		verify(repository, times(1)).executeQuery(eq(message2));
		verify(sqsResponder, times(1)).sendResponseMessage(any(MessageContent.class), any(MessageContent.class));
		verify(sqsClient, times(1)).deleteMessage(any(DeleteMessageRequest.class));
	}

	@Test
	void pollQueueContinuously_ShouldContinuePollingUntilInterrupted() throws InterruptedException {
		ProductsServiceService spy = spy(serviceUnderTest);

		doNothing().doNothing().doThrow(new RuntimeException("Stop the test"))
				.when(spy).pollQueue();

		assertThrows(RuntimeException.class, () -> spy.pollQueueContinuously());
		verify(spy, times(3)).pollQueue();
	}

	@Test
	void initializePolling_ShouldStartNewThread() throws InterruptedException {
		ProductsServiceService spy = spy(serviceUnderTest);

		doNothing().when(spy).pollQueueContinuously();

		spy.initializePolling();

		Thread.sleep(100);

		verify(spy, times(1)).pollQueueContinuously();
	}
}