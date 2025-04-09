package com.mcastillo.productsService.repository;

import com.amazonaws.services.sqs.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcastillo.Product;
import com.mcastillo.productsService.configuration.Queries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

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
        String response;

        switch (action){
            case "GET":

                List<Product> productList = jdbcTemplate.query(queries.getSelectAllProducts(), new ProductRowMapper());

                // serialize the list obtained from the database
                try {
                    response = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(productList);
                } catch (Exception e){
                    logger.error("Error serializing product list:", e);
                    response = "Error serializing product list";
                }
                break;

            case "POST":
                // Deserialize the product from the message body
                Product product;
                try {
                    product = objectMapper.readValue(message.getBody(), Product.class);
                    logger.info("Creating product: {}", product);
                    jdbcTemplate.update(queries.getCreateProduct(),
                            product.getName(),
                            product.getDescription(),
                            product.getPrice(),
                            product.getExpirationDate());
                    response = "Product created: " + product.getName();

                } catch (Exception e){
                    logger.error("Error serializing product list:", e);
                    response = "Error deserializing product from POST";
                }
                break;

            case "PUT":
                // Deserialize the product from the message body
                Product updatedProduct;
                try {
                    updatedProduct = objectMapper.readValue(message.getBody(), Product.class);
                    jdbcTemplate.update(queries.getUpdateProduct(),
                            updatedProduct.getName(),
                            updatedProduct.getDescription(),
                            updatedProduct.getPrice(),
                            updatedProduct.getExpirationDate(),
                            updatedProduct.getId());

                    response = "Product updated: " + updatedProduct.getName();
                } catch (Exception e) {
                    logger.error("Error serializing product list:", e);
                    response = "Error deserializing product from PUT";
                }
                break;

            case "DELETE":
                int id = Integer.parseInt(message.getBody());
                logger.info("Deleting product with id: {}", id);
                jdbcTemplate.update(queries.getDeleteProduct(), id);
                response = "Product deleted with id: " + id;
                break;

            default:
                logger.info("Action not supported");
                response = "Action not supported";
                break;
        }

        return response;
    }

}
