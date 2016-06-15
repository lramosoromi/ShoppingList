package com.mobilecomputing.shoppinglist;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by rolithunderbird on 11.06.16.
 */
public class ProductsList {
    private Long id;
    private String name;
    private List<Product> products;
    private List<Date> expiryDates;


    public ProductsList() {
        products = new ArrayList<>();
        expiryDates = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public List<Date> getExpiryDates() {
        return expiryDates;
    }

    public void setExpiryDates(List<Date> expiryDates) {
        this.expiryDates = expiryDates;
    }

    // Will be used by the ArrayAdapter in the ListView
    @Override
    public String toString() {
        return name;
    }
}
