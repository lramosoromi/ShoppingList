package com.mobilecomputing.shoppinglist;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by rolithunderbird on 11.06.16.
 */
public class MySQLHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "shoppingList.db";
    private static final int DATABASE_VERSION = 1;

    //Table products
    public static final String TABLE_PRODUCTS = "products";
    public static final String COLUMN_ID_PRODUCTS = "id";
    public static final String COLUMN_NAME_PRODUCTS = "name";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_CALORIES = "calories";
    public static final String COLUMN_ORGANIC = "organic";

    //Table lists
    public static final String TABLE_LIST = "lists";
    public static final String COLUMN_ID_LIST = "_id";
    public static final String COLUMN_NAME_LIST = "name";
    public static final String COLUMN_PRODUCTS = "products";
    public static final String COLUMN_EXPIRATION_DATE = "expiry_date";

    //Table grocery_stores
    public static final String TABLE_GROCERY_STORES = "grocery_stores";
    public static final String COLUMN_ID_GROCERY_STORE = "_id";
    public static final String COLUMN_NAME_GROCERY_STORE = "name";
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_COORDINATES = "coordinates";

    // Database creation sql statement
    private static final String DATABASE_CREATE_PRODUCTS = "create table "
            + TABLE_PRODUCTS + " ("
            + COLUMN_ID_PRODUCTS + " integer primary key not null, "
            + COLUMN_NAME_PRODUCTS + " text not null, "
            + COLUMN_PRICE + " integer, "
            + COLUMN_CALORIES + " integer, "
            + COLUMN_ORGANIC + " integer"
            +");";
    private static final String DATABASE_CREATE_LISTS = "create table "
            + TABLE_LIST + " ("
            + COLUMN_ID_LIST + " integer primary key autoincrement, "
            + COLUMN_NAME_LIST + " text not null, "
            + COLUMN_PRODUCTS + " text, "
            + COLUMN_EXPIRATION_DATE + " text "
            +");";
    private static final String DATABASE_CREATE_GROCERY_STORES = "create table "
            + TABLE_GROCERY_STORES + " ("
            + COLUMN_ID_GROCERY_STORE + " integer primary key autoincrement, "
            + COLUMN_NAME_GROCERY_STORE + " text not null, "
            + COLUMN_ADDRESS + " text, "
            + COLUMN_COORDINATES + " text "
            +");";

    public MySQLHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE_PRODUCTS);
        database.execSQL(DATABASE_CREATE_LISTS);
        database.execSQL(DATABASE_CREATE_GROCERY_STORES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LIST);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROCERY_STORES);
        onCreate(db);
    }
}