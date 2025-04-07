package com.mcastillo;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.sql.Date;

public class Product {
    private int id;
    private String name;
    private String description;
    private float price;

    // the postgres database stores dates in this format
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date expirationDate;

    public Product() {
    }

    public Product(int id, String name, String description, float price, Date expirationDate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.expirationDate = expirationDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price){
        this.price = price;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    @Override
    public String toString(){
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", expirationDate=" + expirationDate +
                '}';
    }
}
