package com.mobilecomputing.shoppinglist;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by rolithunderbird on 11.06.16.
 */
public class Product {
    private long id;
    private String name;
    //private Date expiryDate;
    private int price;
    private int calories;
    private boolean organic;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

/*
    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }
*/

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public boolean isOrganic() {
        return organic;
    }

    public void setOrganic(boolean organic) {
        this.organic = organic;
    }

    public JSONObject getJSONObject() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("Id", getId());
            obj.put("Name", getName());
            obj.put("Price", getPrice());
            obj.put("Calories", getCalories());
            obj.put("Organic", isOrganic());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static Product getObjectFromJSON(String productString) {
        Product product = new Product();
        String[] splitString = productString.split(",");

        for (String oneSplitString : splitString) {
            String[] keyValuePair = oneSplitString.split(":");
            String keyString = keyValuePair[0].replaceAll("[^A-Za-z]+", "");
            String valueString = keyValuePair[1].replaceAll("[^A-Za-z]+", "");
            switch (keyString) {
                case "Id" :
                    valueString = keyValuePair[1].replaceAll("\\D+","");
                    product.setId(Long.parseLong(valueString));
                    break;
                case "Name" :
                    product.setName(valueString);
                    break;
                case "Price" :
                    valueString = keyValuePair[1].replaceAll("\\D+","");
                    product.setPrice(Integer.parseInt(valueString));
                    break;
                case "Calories" :
                    valueString = keyValuePair[1].replaceAll("\\D+","");
                    product.setCalories(Integer.parseInt(valueString));
                    break;
                case "Organic" :
                    product.setOrganic(valueString.equals("true"));
                    break;
                default:
                    break;
            }
        }
        return product;
    }

    // Will be used by the ArrayAdapter in the ListView
    @Override
    public String toString() {
        return name;
    }
}