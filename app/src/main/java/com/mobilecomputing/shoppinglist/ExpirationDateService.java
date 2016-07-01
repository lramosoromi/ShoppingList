package com.mobilecomputing.shoppinglist;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * This service looks for all the products in the users inventory and creates a notification if
 * any of them is about to expire.
 *
 * Created by rolithunderbird on 17.06.16.
 */
public class ExpirationDateService extends IntentService {

    // An ID used to post the notification.
    public static final int NOTIFICATION_ID = 1;


    public ExpirationDateService() {
        super("ExpirationDateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        ProductsListDataSource productsListDataSource = new ProductsListDataSource(this);
        productsListDataSource.open();

        Calendar calendar = Calendar.getInstance();
        ProductsList inventory = productsListDataSource.getInventory();

        // BEGIN_INCLUDE(service_onhandle)

        //Check the expiration dates
        ArrayList<Product> products;
        ArrayList<Date> expiryDates;
        ArrayList<Product> productsToExpire = new ArrayList<>();

        products = (ArrayList<Product>) inventory.getProducts();
        expiryDates = (ArrayList<Date>) inventory.getExpiryDates();

        for (int i = 0; i < expiryDates.size(); i++) {
            Date today = new Date(System.currentTimeMillis());
            calendar.setTime(today);
            calendar.add(Calendar.DAY_OF_MONTH, 3);
            if (expiryDates.get(i).before(calendar.getTime())) {
                productsToExpire.add(products.get(i));
            }
        }

        if (productsToExpire.size() > 0)
            sendProductNotification("Products about to expire");

        GroceryStoresService groceryStoresService = new GroceryStoresService(this);
        groceryStoresService.start(intent);

        // Release the wake lock provided by the BroadcastReceiver.
        AlarmReceiver.completeWakefulIntent(intent);
        // END_INCLUDE(service_onhandle)
    }

    // Post a notification indicating whether a doodle was found.
    private void sendProductNotification(String msg) {
        int requestID = (int) System.currentTimeMillis();
        Intent notificationIntent = new Intent(this, NotificationResultActivity.class);

        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(
                this, requestID,notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(getString(R.string.expiration_alert))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);

        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}