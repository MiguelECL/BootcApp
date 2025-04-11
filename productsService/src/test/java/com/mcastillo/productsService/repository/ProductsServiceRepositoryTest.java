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
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.Date;
import java.sql.PreparedStatement;
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
		String jsonProduct = new ObjectMapper().writeValueAsString(product);

		Map<String, Object> keys = new HashMap<>();
		keys.put("id", 1);

		when(objectMapper.readValue(anyString(), eq(Product.class))).thenReturn(product);
		when(objectMapper.writeValueAsString(any(Product.class))).thenReturn(jsonProduct);
		when(queries.getCreateProduct()).thenReturn("INSERT INTO products (name, description, price, expiration_date) VALUES (?,?,?,?)");

		PreparedStatement mockPs = mock(PreparedStatement.class);
		java.sql.Connection mockConn = mock(java.sql.Connection.class);

		try {
			when(mockConn.prepareStatement(anyString(), anyInt())).thenReturn(mockPs);
		} catch (SQLException e) {
			fail("SQLException in test setup");
		}

		doAnswer(invocation -> {
			PreparedStatementCreator psc = invocation.getArgument(0);
			KeyHolder keyHolder = invocation.getArgument(1);

			psc.createPreparedStatement(mockConn);

			((GeneratedKeyHolder) keyHolder).getKeyList().add(keys);

			return 1;
		}).when(jdbcTemplate).update(any(PreparedStatementCreator.class), any(KeyHolder.class));

		Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
		messageAttributes.put("action", new MessageAttributeValue()
				.withDataType("String")
				.withStringValue("POST"));

		Message message = new Message()
				.withMessageAttributes(messageAttributes)
				.withBody(jsonProduct);

		String response = repository.executeQuery(message);
		assertEquals(jsonProduct, response);

		try {
			verify(mockPs).setString(1, product.getName());
			verify(mockPs).setString(2, product.getDescription());
			verify(mockPs).setFloat(3, product.getPrice());
			verify(mockPs).setDate(4, product.getExpirationDate());
		} catch (SQLException e) {
			fail("SQLException during verification");
		}

		verify(jdbcTemplate).update(any(PreparedStatementCreator.class), any(KeyHolder.class));
		verify(queries).getCreateProduct();
	}

	@Test
	void test_executeQuery_POST_Failure() throws JsonProcessingException {
		Product product = new Product(1, "Product 1", "Description 1", 10.0f, Date.valueOf("2023-10-01"));
		String jsonProduct = new ObjectMapper().writeValueAsString(product);

		when(objectMapper.readValue(anyString(), eq(Product.class))).thenReturn(product);

		doAnswer(invocation -> {
			KeyHolder keyHolder = invocation.getArgument(1);
			return 1;
		}).when(jdbcTemplate).update(any(PreparedStatementCreator.class), any(KeyHolder.class));

		Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
		messageAttributes.put("action", new MessageAttributeValue()
				.withDataType("String")
				.withStringValue("POST"));

		Message message = new Message()
				.withMessageAttributes(messageAttributes)
				.withBody(jsonProduct);

		String response = repository.executeQuery(message);
		assertEquals("Error creating product", response);

		verify(jdbcTemplate).update(any(PreparedStatementCreator.class), any(KeyHolder.class));
		verify(objectMapper).readValue(anyString(), eq(Product.class));

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

		assertThrows(RuntimeException.class, ()-> repository.executeQuery(message));
	}

	@Test
	void test_executeQuery_PUT() throws JsonProcessingException {

		// Setup test data
		Product product = new Product(1, "Product 1", "Description 1", 10.0f, Date.valueOf("2023-10-01"));
		String jsonProduct = new ObjectMapper().writeValueAsString(product);

		// Mock dependencies
		when(objectMapper.readValue(anyString(), eq(Product.class))).thenReturn(product);
		when(objectMapper.writeValueAsString(any(Product.class))).thenReturn(jsonProduct);
		when(queries.getUpdateProduct()).thenReturn("UPDATE products SET name=?, description=?, price=?, expiration_date=? WHERE id=?");
		when(jdbcTemplate.update(
				anyString(),
				eq(product.getName()),
				eq(product.getDescription()),
				eq(product.getPrice()),
				eq(product.getExpirationDate()),
				eq(product.getId())
		)).thenReturn(1);

		// Create test message
		Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
		messageAttributes.put("action", new MessageAttributeValue()
				.withDataType("String")
				.withStringValue("PUT"));

		Message message = new Message()
				.withMessageAttributes(messageAttributes)
				.withBody(jsonProduct);

		// Execute and verify
		String response = repository.executeQuery(message);
		assertEquals(jsonProduct, response);

		// Verify interactions
		verify(jdbcTemplate).update(
				anyString(),
				eq(product.getName()),
				eq(product.getDescription()),
				eq(product.getPrice()),
				eq(product.getExpirationDate()),
				eq(product.getId())
		);
		verify(objectMapper).readValue(anyString(), eq(Product.class));
		verify(objectMapper).writeValueAsString(any(Product.class));
		verify(queries).getUpdateProduct();
	}

	@Test
	void test_executeQuery_PUT_Failure() throws JsonProcessingException {

		// Setup test data
		Product product = new Product(1, "Product 1", "Description 1", 10.0f, Date.valueOf("2023-10-01"));
		String jsonProduct = new ObjectMapper().writeValueAsString(product);

		// Mock dependencies
		when(objectMapper.readValue(anyString(), eq(Product.class))).thenReturn(product);
		when(queries.getUpdateProduct()).thenReturn("UPDATE products SET name=?, description=?, price=?, expiration_date=? WHERE id=?");
		when(jdbcTemplate.update(
				anyString(),
				eq(product.getName()),
				eq(product.getDescription()),
				eq(product.getPrice()),
				eq(product.getExpirationDate()),
				eq(product.getId())
		)).thenReturn(0); // Return 0 to simulate no rows affected

		// Create test message
		Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
		messageAttributes.put("action", new MessageAttributeValue()
				.withDataType("String")
				.withStringValue("PUT"));

		Message message = new Message()
				.withMessageAttributes(messageAttributes)
				.withBody(jsonProduct);

		// Execute and verify
		String response = repository.executeQuery(message);
		assertEquals("Failure to update from database", response);

		// Verify interactions
		verify(jdbcTemplate).update(
				anyString(),
				eq(product.getName()),
				eq(product.getDescription()),
				eq(product.getPrice()),
				eq(product.getExpirationDate()),
				eq(product.getId())
		);
		verify(objectMapper).readValue(anyString(), eq(Product.class));
		verify(queries).getUpdateProduct();
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

		assertEquals("Error serializing product list", response);
	}

	@Test
	void test_executeQuery_DELETE(){

		Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
		messageAttributes.put("action", new MessageAttributeValue()
				.withDataType("String")
				.withStringValue("DELETE"));

		Message message = new Message()
				.withMessageAttributes(messageAttributes)
				.withBody("2");

		when(queries.getDeleteProduct()).thenReturn("DELETE FROM products WHERE id = ?");
		when(jdbcTemplate.update(anyString(), eq(2))).thenReturn(1);

		String response = repository.executeQuery(message);

		assertEquals("Product deleted with id: 2", response);
		verify(jdbcTemplate).update(anyString(), eq(2));
		verify(queries).getDeleteProduct();
	}

	@Test
	void test_executeQuery_DELETE_failure(){

		Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
		messageAttributes.put("action", new MessageAttributeValue()
				.withDataType("String")
				.withStringValue("DELETE"));

		Message message = new Message()
				.withMessageAttributes(messageAttributes)
				.withBody("2");

		when(queries.getDeleteProduct()).thenReturn("DELETE FROM products WHERE id = ?");
		when(jdbcTemplate.update(anyString(), eq(2))).thenReturn(0);

		String response = repository.executeQuery(message);

		assertEquals("Failure to delete from database", response);
		verify(jdbcTemplate).update(anyString(), eq(2));
		verify(queries).getDeleteProduct();
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