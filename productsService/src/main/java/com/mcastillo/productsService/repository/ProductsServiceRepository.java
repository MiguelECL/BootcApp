package com.mcastillo.productsService.repository;

import com.amazonaws.services.sqs.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcastillo.Product;
import com.mcastillo.productsService.configuration.Queries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class ProductsServiceRepository {

    private final Queries queries;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
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
        System.out.println(action);
        String response;

        switch (action){
            case "GET":

                List<Product> productList = jdbcTemplate.query(queries.getSelectAllProducts(), new ProductRowMapper());

                // serialize the list obtained from the database
                try {
                    response = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(productList);
                } catch (Exception e){
                    e.printStackTrace();
                    response = "Error serializing product list";
                }
                break;

            case "POST":
                // Deserialize the product from the message body
                Product product;
                try {
                    product = objectMapper.readValue(message.getBody(), Product.class);
                    System.out.println("Creating product: " + product);
                    jdbcTemplate.update(queries.getCreateProduct(),
                            product.getName(),
                            product.getDescription(),
                            product.getPrice(),
                            product.getExpirationDate());
                    response = "Product created: " + product.getName();

                } catch (Exception e){
                    e.printStackTrace();
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
                    e.printStackTrace();
                    response = "Error deserializing product from PUT";
                }
                break;

            case "DELETE":
                int id = Integer.parseInt(message.getBody());
                System.out.println("Deleting product with id: " + id);
                jdbcTemplate.update(queries.getDeleteProduct(), id);
                response = "Product deleted with id: " + id;
                break;

            default:
                System.out.println("Action not supported");
                response = "Action not supported";
                break;
        }

        return response;
    }

}
