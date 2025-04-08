package com.mcastillo.productsService.repository;

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mcastillo.Product;
import com.mcastillo.productsService.configuration.Queries;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductsServiceRepositoryTest {

	@Mock
	private Queries queries;

	@Mock
	private JdbcTemplate jdbcTemplate;

	@Mock
	private ObjectMapper objectMapper;

	private ProductsServiceRepository repository;

	@BeforeEach
	void setUp(){
		repository = new ProductsServiceRepository(queries, objectMapper, jdbcTemplate);
	}

	@Test
	public void test_ProductRowMapper() throws SQLException {
		ResultSet mockResultSet = mock(ResultSet.class);

		when(mockResultSet.getInt("id")).thenReturn(101);
		when(mockResultSet.getString("name")).thenReturn("Test Product");
		when(mockResultSet.getString("description")).thenReturn("Test Description");
		when(mockResultSet.getFloat("price")).thenReturn(19.99f);
		when(mockResultSet.getDate("expiration_date")).thenReturn(Date.valueOf("2023-10-01"));

		ProductsServiceRepository.ProductRowMapper mapper = new ProductsServiceRepository.ProductRowMapper();

		Product result = mapper.mapRow(mockResultSet, 1);

		assertNotNull(result);
		assertEquals(101, result.getId());
		assertEquals("Test Product", result.getName());
		assertEquals("Test Description", result.getDescription());
		assertEquals(19.99f, result.getPrice(), 0.001);  // Using delta for float comparison
		assertEquals(Date.valueOf("2023-10-01"), result.getExpirationDate());
	}

	@Test
	void test_executeQuery_GET() throws JsonProcessingException {
		Product product = new Product(1, "Product 1", "Description 1", 10.0f, Date.valueOf("2023-10-01"));
		List<Product> productList = Collections.singletonList(product);

		when(queries.getSelectAllProducts()).thenReturn("SELECT * FROM products");
		when(jdbcTemplate.query(anyString(), any(ProductsServiceRepository.ProductRowMapper.class))).thenReturn(productList);

		ObjectWriter mockWriter = mock(ObjectWriter.class);
		when(objectMapper.writerWithDefaultPrettyPrinter()).thenReturn(mockWriter);
		when(mockWriter.writeValueAsString(productList)).thenReturn("mocked Json");

		Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
		messageAttributes.put("action", new MessageAttributeValue()
				.withDataType("String")
				.withStringValue("GET"));

		Message message = new Message().withMessageAttributes(messageAttributes);

		String response = repository.executeQuery(message);

		assertEquals("mocked Json", response);
	}

	@Test
	void test_executeQuery_GET_Exception() throws JsonProcessingException {
		Product product = new Product(1, "Product 1", "Description 1", 10.0f, Date.valueOf("2023-10-01"));
		List<Product> productList = Collections.singletonList(product);

		Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
		messageAttributes.put("action", new MessageAttributeValue()
				.withDataType("String")
				.withStringValue("GET"));

		Message message = new Message().withMessageAttributes(messageAttributes);

		ObjectWriter mockWriter = mock(ObjectWriter.class);
		when(objectMapper.writerWithDefaultPrettyPrinter()).thenReturn(mockWriter);
		when(mockWriter.writeValueAsString(any())).thenThrow(new JsonProcessingException("Error serializing product list") {});
		when(queries.getSelectAllProducts()).thenReturn("SELECT * FROM products");
		when(jdbcTemplate.query(anyString(), any(ProductsServiceRepository.ProductRowMapper.class))).thenReturn(productList);

		String response = repository.executeQuery(message);

		assertEquals("Error serializing product list", response);
	}

	@Test
	void test_executeQuery_POST() throws JsonProcessingException {

		Product product = new Product(1, "Product 1", "Description 1", 10.0f, Date.valueOf("2023-10-01"));

		ObjectWriter mockWriter = mock(ObjectWriter.class);
		when(objectMapper.writerWithDefaultPrettyPrinter()).thenReturn(mockWriter);
		when(mockWriter.writeValueAsString(product)).thenReturn("mocked-json");

		when(objectMapper.readValue("mocked-json", Product.class)).thenReturn(product);

		Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
		messageAttributes.put("action", new MessageAttributeValue()
				.withDataType("String")
				.withStringValue("POST"));

		Message message = new Message().withMessageAttributes(messageAttributes)
				.withBody(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(product));

		String response = repository.executeQuery(message);

		assertEquals("Product created: " + product.getName(), response);
	}

	@Test
	void test_executeQuery_POST_Exception() throws JsonProcessingException {
		Product product = new Product(1, "Product 1", "Description 1", 10.0f, Date.valueOf("2023-10-01"));

		String invalidJson = "{ \"invalid\": \"data\" }";

		Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
		messageAttributes.put("action", new MessageAttributeValue()
				.withDataType("String")
				.withStringValue("POST"));

		Message message = new Message().withMessageAttributes(messageAttributes);
		message.setBody(invalidJson);

		when(objectMapper.readValue(invalidJson, Product.class)).thenThrow(new JsonProcessingException("Error deserializing product from POST") {});

		String response = repository.executeQuery(message);

		assertEquals("Error deserializing product from POST", response);
	}

	@Test
	void test_executeQuery_PUT() throws JsonProcessingException {

		Product product = new Product(1, "Product 1", "Description 1", 10.0f, Date.valueOf("2023-10-01"));

		ObjectWriter mockWriter = mock(ObjectWriter.class);
		when(objectMapper.writerWithDefaultPrettyPrinter()).thenReturn(mockWriter);
		when(mockWriter.writeValueAsString(product)).thenReturn("mocked-json");

		when(objectMapper.readValue("mocked-json", Product.class)).thenReturn(product);

		Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
		messageAttributes.put("action", new MessageAttributeValue()
				.withDataType("String")
				.withStringValue("PUT"));

		Message message = new Message().withMessageAttributes(messageAttributes)
				.withBody(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(product));

		String response = repository.executeQuery(message);
		assertEquals("Product updated: " + product.getName(), response);
	}

	@Test
	void test_executeQuery_PUT_Exception() throws JsonProcessingException {
		String invalidJson = "{ \"invalid\": \"data\" }";

		Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
		messageAttributes.put("action", new MessageAttributeValue()
				.withDataType("String")
				.withStringValue("PUT"));

		Message message = new Message().withMessageAttributes(messageAttributes);
		message.setBody(invalidJson);

		when(objectMapper.readValue(invalidJson, Product.class)).thenThrow(new JsonProcessingException("Error deserializing product from PUT") {});

		String response = repository.executeQuery(message);

		assertEquals("Error deserializing product from PUT", response);
	}

	@Test
	void test_executeQuery_DELETE(){

		Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
		messageAttributes.put("action", new MessageAttributeValue()
				.withDataType("String")
				.withStringValue("DELETE"));

		Message message = new Message().withMessageAttributes(messageAttributes)
				.withBody("2");

		String response = repository.executeQuery(message);

		repository.executeQuery(message);
		assertEquals("Product deleted with id: 2", response);
	}

	@Test
	void test_executeQuery_Default(){

		Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
		messageAttributes.put("action", new MessageAttributeValue()
				.withDataType("String")
				.withStringValue("NOT_A_VALID_ACTION"));

		Message message = new Message().withMessageAttributes(messageAttributes)
				.withBody("Invalid message body");

		String response = repository.executeQuery(message);

		assertEquals("Action not supported", response);


	}
}