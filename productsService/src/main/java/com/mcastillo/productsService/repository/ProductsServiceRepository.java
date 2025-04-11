package com.mcastillo.productsService.repository;

import com.amazonaws.services.sqs.model.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcastillo.DatabaseException;
import com.mcastillo.Product;
import com.mcastillo.productsService.configuration.Queries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository
public class ProductsServiceRepository {

    private static final Logger logger = LoggerFactory.getLogger(ProductsServiceRepository.class);
    private final Queries queries;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public ProductsServiceRepository(Queries queries, ObjectMapper objectMapper, JdbcTemplate jdbcTemplate) {
        this.queries = queries;
        this.objectMapper = objectMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    // Custom row mapper
    public static class ProductRowMapper implements RowMapper<Product> {
        @Override
        public Product mapRow(ResultSet rs, int row) throws SQLException {
            Product product = new Product();
            product.setId(rs.getInt("id"));
            product.setName(rs.getString("name"));
            product.setDescription(rs.getString("description"));
            product.setPrice(rs.getFloat("price"));
            product.setExpirationDate(rs.getDate("expiration_date"));

            return product;
        }
    }

    public String executeQuery(Message message){
        String action = message.getMessageAttributes().get("action").getStringValue();
        logger.info("Received action from message: {} ", action);
        String response = "";

        switch (action){
            case "GET":

                List<Product> productList = jdbcTemplate.query(queries.getSelectAllProducts(), new ProductRowMapper());

                try {
                    response = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(productList);
                } catch (Exception e){
                    logger.error("Error serializing product list:", e);
                    response = "Error serializing product list";
                }
                break;

            case "POST":

                Product product;
                Product createdProduct;
                KeyHolder keyHolder = new GeneratedKeyHolder();

                try {
                    product = objectMapper.readValue(message.getBody(), Product.class);
                    logger.info("Creating product: {}", product);

                    jdbcTemplate.update(connection -> {
                        PreparedStatement preparedStatement = connection.prepareStatement(queries.getCreateProduct(), PreparedStatement.RETURN_GENERATED_KEYS);
                        preparedStatement.setString(1, product.getName());
                        preparedStatement.setString(2, product.getDescription());
                        preparedStatement.setFloat(3, product.getPrice());
                        preparedStatement.setDate(4, product.getExpirationDate());
                        return preparedStatement;
                    }, keyHolder);

                    Map<String, Object> keys = keyHolder.getKeys();

                    if (keys != null) {
                        int id = (int) keys.get("id");
                        System.out.println("NOT NULL");
                        createdProduct = new Product(id, product.getName(), product.getDescription(), product.getPrice(), product.getExpirationDate());
                        response = objectMapper.writeValueAsString(createdProduct);
                    } else {
                        logger.error("Error creating product!");
                    }

                } catch (JsonProcessingException e) {
	                throw new RuntimeException("Error creating product", e);
                }
	            break;

            case "PUT":
                // Deserialize the product from the message body
                Product updatedProduct;
                try {
                    updatedProduct = objectMapper.readValue(message.getBody(), Product.class);
                    int rowsAffected = jdbcTemplate.update(queries.getUpdateProduct(),
                            updatedProduct.getName(),
                            updatedProduct.getDescription(),
                            updatedProduct.getPrice(),
                            updatedProduct.getExpirationDate(),
                            updatedProduct.getId());

                    if (rowsAffected > 0){
                        response = "Product updated: " + updatedProduct.getName();
                    } else {
                        response = "Failure to update from database";
                    }
                } catch (Exception e) {
                    logger.error("Error serializing product list:", e);
                }
                break;

            case "DELETE":
                int id = Integer.parseInt(message.getBody());
                logger.info("Deleting product with id: {}", id);
                int rowsAffected = jdbcTemplate.update(queries.getDeleteProduct(), id);
                if (rowsAffected > 0){
                    response = "Product deleted with id: " + id;
                } else {
                    response = "Failure to delete from database";
                    logger.info("Failure to delete from database");
                }
                break;

            default:
                logger.info("Action not supported");
                break;
        }

        return response;
    }

}
