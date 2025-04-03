package com.mcastillo.productsService.repository;

import com.mcastillo.productsService.configuration.Queries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProductsServiceRepository {

    @Autowired
    Queries queries;

    // Using jdbcTemplate;
    @Autowired
    JdbcTemplate jdbcTemplate;

    public void executeQuery(String action){
        switch (action){
            case "GET":
                jdbcTemplate.;
                break;

            default:
                System.out.println("Action not supported");
                break;
        }

    }

}
