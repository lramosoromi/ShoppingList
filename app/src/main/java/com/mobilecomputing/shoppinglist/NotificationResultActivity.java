package com.mobilecomputing.shoppinglist;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class NotificationResultActivity extends ListActivity {

    private ProductsListDataSource datasource;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_result);

        datasource = new ProductsListDataSource(this);
        datasource.open();
        Calendar calendar = Calendar.getInstance();
        ProductsList inventory = datasource.getInventory();
        ArrayList<Product> products;
        ArrayList<Date> expiryDates;
        ArrayList<Product> productsToExpire = new ArrayList<>();


        products = (ArrayList<Product>) inventory.getProducts();
        expiryDates = (ArrayList<Date>) inventory.getExpiryDates();

        for(int i = 0; i < expiryDates.size(); i++) {
            Date today = new Date(System.currentTimeMillis());
            calendar.setTime(today);
            calendar.add(Calendar.DAY_OF_MONTH, 3);
            if (expiryDates.get(i).before(calendar.getTime())) {
                Product productAboutToExpire = products.get(i);
                // Change the name of the products just for this case so as to properly view all the information
                productAboutToExpire.setName("Id: " + productAboutToExpire.getId() +
                        " - Name: " + productAboutToExpire.getName() +
                        " - Exp Date: " + datasource.convertToString(expiryDates.get(i)));
                productsToExpire.add(productAboutToExpire);
            }
        }

        // use the SimpleCursorAdapter to show the
        // elements in a ListView
        ArrayAdapter<Product> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, productsToExpire);
        setListAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}