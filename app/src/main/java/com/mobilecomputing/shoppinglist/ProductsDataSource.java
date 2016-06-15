package com.mobilecomputing.shoppinglist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rolithunderbird on 11.06.16.
 */
public class ProductsDataSource {

    // Database fields
    private SQLiteDatabase database;
    private MySQLHelper dbHelper;
    private String[] allColumns = { MySQLHelper.COLUMN_ID_PRODUCTS,
            MySQLHelper.COLUMN_NAME_PRODUCTS, MySQLHelper.COLUMN_PRICE,
            MySQLHelper.COLUMN_CALORIES, MySQLHelper.COLUMN_ORGANIC};

    public ProductsDataSource(Context context) {
        dbHelper = new MySQLHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Product createProduct(long id, String name, int price, int calories, boolean organic) {
        ContentValues values = new ContentValues();
        values.put(MySQLHelper.COLUMN_ID_PRODUCTS, id);
        values.put(MySQLHelper.COLUMN_NAME_PRODUCTS, name);
        values.put(MySQLHelper.COLUMN_PRICE, price);
        values.put(MySQLHelper.COLUMN_CALORIES, calories);
        if (organic)
            values.put(MySQLHelper.COLUMN_ORGANIC, 1);
        else
            values.put(MySQLHelper.COLUMN_ORGANIC, 0);

        database.insert(MySQLHelper.TABLE_PRODUCTS, null, values);
        Cursor cursor = database.query(MySQLHelper.TABLE_PRODUCTS,
                allColumns, MySQLHelper.COLUMN_ID_PRODUCTS + " = " + id, null,
                null, null, null);
        cursor.moveToFirst();
        Product newProduct = cursorToProduct(cursor);
        cursor.close();
        return newProduct;
    }

    public void deleteProduct(Product product) {
        long id = product.getId();
        System.out.println("Product deleted with id: " + id);
        database.delete(MySQLHelper.TABLE_PRODUCTS, MySQLHelper.COLUMN_ID_PRODUCTS
                + " = " + id, null);
    }

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();

        Cursor cursor = database.query(MySQLHelper.TABLE_PRODUCTS,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Product product = cursorToProduct(cursor);
            products.add(product);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return products;
    }

    public Product getProduct(Long id) {
        Cursor cursor = database.query(MySQLHelper.TABLE_PRODUCTS,
                allColumns, MySQLHelper.COLUMN_ID_PRODUCTS + " = " + id, null, null, null, null);
        cursor.moveToFirst();
        Product searchedProduct = cursorToProduct(cursor);
        cursor.close();
        return searchedProduct;
    }

    private Product cursorToProduct(Cursor cursor) {
        Product product = new Product();
        product.setId(cursor.getLong(0));
        product.setName(cursor.getString(1));
        product.setPrice(cursor.getInt(2));
        product.setCalories(cursor.getInt(3));
        if (cursor.getInt(4) == 0)
            product.setOrganic(false);
        else
            product.setOrganic(true);
        return product;
    }
}