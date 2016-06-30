package com.mobilecomputing.shoppinglist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rolithunderbird on 17.06.16.
 */
public class GroceryStoresDataSource {

    // Database fields
    private SQLiteDatabase database;
    private MySQLHelper dbHelper;
    private String[] allColumns = { MySQLHelper.COLUMN_ID_GROCERY_STORE,
            MySQLHelper.COLUMN_NAME_GROCERY_STORE, MySQLHelper.COLUMN_ADDRESS,
            MySQLHelper.COLUMN_COORDINATES};

    public GroceryStoresDataSource(Context context) {
        dbHelper = new MySQLHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public GroceryStore createGroceryStore(String name, String address, String coordinates) {
        ContentValues values = new ContentValues();
        values.put(MySQLHelper.COLUMN_NAME_GROCERY_STORE, name);
        values.put(MySQLHelper.COLUMN_ADDRESS, address);
        values.put(MySQLHelper.COLUMN_COORDINATES, coordinates);

        long id = database.insert(MySQLHelper.TABLE_GROCERY_STORES, null, values);
        Cursor cursor = database.query(MySQLHelper.TABLE_GROCERY_STORES,
                allColumns, MySQLHelper.COLUMN_ID_GROCERY_STORE + " = " + id, null,
                null, null, null);
        cursor.moveToFirst();
        GroceryStore newGroceryStore = cursorToGroceryStore(cursor);
        cursor.close();
        return newGroceryStore;
    }

    public void deleteGroceryStore(GroceryStore groceryStore) {
        long id = groceryStore.getId();
        System.out.println("Grocery Store deleted with id: " + id);
        database.delete(MySQLHelper.TABLE_GROCERY_STORES, MySQLHelper.COLUMN_ID_GROCERY_STORE
                + " = " + id, null);
    }

    public List<GroceryStore> getAllGroceryStores() {
        List<GroceryStore> groceryStores = new ArrayList<>();

        Cursor cursor = database.query(MySQLHelper.TABLE_GROCERY_STORES,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            GroceryStore groceryStore = cursorToGroceryStore(cursor);
            groceryStores.add(groceryStore);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return groceryStores;
    }

    public GroceryStore getGroceryStore(Long id) {
        Cursor cursor = database.query(MySQLHelper.TABLE_GROCERY_STORES,
                allColumns, MySQLHelper.COLUMN_ID_GROCERY_STORE + " = " + id, null, null, null, null);
        cursor.moveToFirst();
        GroceryStore searchedGroceryStore = cursorToGroceryStore(cursor);
        cursor.close();
        return searchedGroceryStore;
    }

    private GroceryStore cursorToGroceryStore(Cursor cursor) {
        GroceryStore groceryStore = new GroceryStore();

        groceryStore.setId(cursor.getLong(0));
        groceryStore.setName(cursor.getString(1));
        groceryStore.setAddress(cursor.getString(2));
        String[] coordinates = cursor.getString(3).split(",");
        groceryStore.setCoordinates(new LatLng(Float.parseFloat(coordinates[0]),
                                        Float.parseFloat(coordinates[1])));
        return groceryStore;
    }
}
