package com.mobilecomputing.shoppinglist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.dm.zbar.android.scanner.ZBarConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by rolithunderbird on 11.06.16.
 */
public class ProductsListDataSource {

    // Database fields
    private SQLiteDatabase database;
    private MySQLHelper dbHelper;
    private String[] allColumns = { MySQLHelper.COLUMN_ID_LIST, MySQLHelper.COLUMN_NAME_LIST,
            MySQLHelper.COLUMN_PRODUCTS, MySQLHelper.COLUMN_EXPIRATION_DATE};

    //Other fields
    private ProductsDataSource datasource;
    private boolean isProductInDatabase;
    private List<Product> productsList;
    private List<Date> expiryDates;


    public ProductsListDataSource(Context context) {
        dbHelper = new MySQLHelper(context);
        datasource = new ProductsDataSource(context);
        datasource.open();
        productsList = new ArrayList<>();
        expiryDates = new ArrayList<>();
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public ProductsList createList(String name) {
        ContentValues values = new ContentValues();

        values.put(MySQLHelper.COLUMN_NAME_LIST, name);

        long insertId = database.insert(MySQLHelper.TABLE_LIST, null,
                values);
        Cursor cursor = database.query(MySQLHelper.TABLE_LIST,
                allColumns, MySQLHelper.COLUMN_ID_LIST + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        ProductsList newProductsList = null;
        try {
            newProductsList = cursorToProductsList(cursor);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        cursor.close();
        return newProductsList;
    }

    public void deleteList(ProductsList list) {
        long id = list.getId();
        System.out.println("List deleted with id: " + id);
        database.delete(MySQLHelper.TABLE_LIST, MySQLHelper.COLUMN_ID_LIST
                + " = " + id, null);
    }

    public List<ProductsList> getAllLists() {
        List<ProductsList> productsLists = new ArrayList<>();

        Cursor cursor = database.query(MySQLHelper.TABLE_LIST,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ProductsList productsList = null;
            try {
                productsList = cursorToProductsList(cursor);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            productsLists.add(productsList);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return productsLists;
    }

    private ProductsList cursorToProductsList(Cursor cursor) throws JSONException {
        JSONObject json;
        JSONArray jsonArray;
        ArrayList<Product> productItems;
        ArrayList expiresItems;

        ProductsList productsList = new ProductsList();
        productsList.setId(cursor.getLong(0));
        productsList.setName(cursor.getString(1));
        if (cursor.getString(2) != null) {
            json = new JSONObject(cursor.getString(2));
            productItems = new ArrayList<>();
            jsonArray = json.optJSONArray("productsArray");
            if (jsonArray != null) {
                int len = jsonArray.length();
                for (int i = 0; i < len; i++) {
                    productItems.add(Product.getObjectFromJSON(jsonArray.get(i).toString()));
                }
            }
            productsList.setProducts(productItems);
        }

        if (cursor.getString(3) != null) {
            json = new JSONObject(cursor.getString(3));
            expiresItems = new ArrayList();
            jsonArray = json.optJSONArray("expiresArrays");
            if (jsonArray != null) {
                int len = jsonArray.length();
                for (int i = 0; i < len; i++) {
                    expiresItems.add(convertStringToDate(jsonArray.get(i).toString()));
                }
            }
            productsList.setExpiryDates(expiresItems);
        }
        return productsList;
    }

    public Product addProductToList(long listId, long productId, String name, int price,
                                 int calories, String organic, Date expiryDate) throws JSONException {
        Product product;
        ProductsList newProductsList = getProductList(listId);
        JSONObject productJsonObj = new JSONObject();
        JSONArray productsArray = new JSONArray();
        JSONObject expirationJsonObj = new JSONObject();

        if (isProductInDatabase) {
            product = searchProduct(productId);
        }
        else {
            boolean blnOrganic;
            blnOrganic = organic.equals("true");
            product = datasource.createProduct(productId, name, price, calories, blnOrganic);
        }

        //CUANDO HAGO EL GETPRODUCTS ME TIRA QUE GETPRODUCTS DA NULL
        newProductsList.getProducts().add(product);
        newProductsList.getExpiryDates().add(expiryDate);

        ContentValues values = new ContentValues();
        values.put(MySQLHelper.COLUMN_NAME_LIST, newProductsList.getName());

        ArrayList<Product> products = (ArrayList<Product>) newProductsList.getProducts();
        for (int i = 0; i < products.size(); i++) {
            productsArray.put(products.get(i).getJSONObject());
        }
        productJsonObj.put("productsArray", productsArray);
        String arrayListProducts = productJsonObj.toString();
        values.put(MySQLHelper.COLUMN_PRODUCTS, arrayListProducts);

        ArrayList<String> expirationDatesAsStringList = new ArrayList<>();
        for (Date date : newProductsList.getExpiryDates()) {
            expirationDatesAsStringList.add(convertToString(date));
        }
        expirationJsonObj.put("expiresArrays", new JSONArray(expirationDatesAsStringList));
        String arrayListExpires = expirationJsonObj.toString();
        values.put(MySQLHelper.COLUMN_EXPIRATION_DATE, arrayListExpires);

        database.update(MySQLHelper.TABLE_LIST, values, MySQLHelper.COLUMN_ID_LIST + "=" + listId, null);

        return product;
    }

    public Product searchProduct(long id) {
        List<Product> productList;
        isProductInDatabase = false;

        productList = datasource.getAllProducts();
        for (int i = 0; i < productList.size(); i++) {
            if (productList.get(i).getId() == id) {
                isProductInDatabase = true;
                break;
            }
        }
        if (isProductInDatabase) {
            return datasource.getProduct(id);
        }
        else {
            return null;
        }
    }

    public List<Product> getAllProductsInList(long id) {
        List<Product> listProducts;

        ProductsList productsList = getProductList(id);
        if (productsList.getProducts() == null)
            listProducts = new ArrayList<>();
        else
            listProducts = productsList.getProducts();

        return listProducts;
    }

    public void deleteProductFromList(long id, Product product) throws JSONException {
        ProductsList productsList = getProductList(id);

        productsList.getProducts().remove(product);

        ContentValues values = new ContentValues();

        JSONObject json = new JSONObject();
        json.put("productsArray", new JSONArray(productsList.getProducts()));
        String arrayListProducts = json.toString();
        values.put(MySQLHelper.COLUMN_PRODUCTS, arrayListProducts);

        database.update(MySQLHelper.TABLE_LIST, values, MySQLHelper.COLUMN_ID_LIST + "=" + id, null);
    }

    private ProductsList getProductList(long id) {
        Cursor cursor = database.query(MySQLHelper.TABLE_LIST,
                allColumns, MySQLHelper.COLUMN_ID_LIST + " = " + id, null,
                null, null, null);
        cursor.moveToFirst();
        ProductsList newProductsList = null;
        try {
            newProductsList = cursorToProductsList(cursor);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        cursor.close();
        return newProductsList;
    }

    public Date convertStringToDate(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
//        String date = sdf.format(new Date());
        Date parsedDate;
        try {
            parsedDate = sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            parsedDate = null;
        }
        return parsedDate;
    }

    private String convertToString(Date expiryDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(expiryDate);
    }
}
