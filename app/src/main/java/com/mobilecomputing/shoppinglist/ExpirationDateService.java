package com.mobilecomputing.shoppinglist;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

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
        super("SchedulingService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        ProductsListDataSource datasource = new ProductsListDataSource(this);
        datasource.open();
        Calendar calendar = Calendar.getInstance();
        ProductsList inventory = datasource.getInventory();

        // BEGIN_INCLUDE(service_onhandle)
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
                productsToExpire.add(products.get(i));
            }
        }

        if (productsToExpire.size() > 0)
            sendNotification("Products about to expire");

        // Release the wake lock provided by the BroadcastReceiver.
        AlarmReceiver.completeWakefulIntent(intent);
        // END_INCLUDE(service_onhandle)
    }

    // Post a notification indicating whether a doodle was found.
    private void sendNotification(String msg) {
        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, NotificationResultActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(getString(R.string.expiration_alert))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}