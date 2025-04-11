package com.mcastillo.productsService.service;

import com.amazonaws.services.sqs.model.Message;

import javax.annotation.PostConstruct;

public interface ProductsServiceService {
	@PostConstruct
	void initializePolling();

	void pollQueue();

	void processMessage(Message message);

	void deleteMessage(Message message);
}
