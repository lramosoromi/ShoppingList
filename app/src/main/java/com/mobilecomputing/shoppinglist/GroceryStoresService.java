package com.mobilecomputing.shoppinglist;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

/**
 * Created by rolithunderbird on 30.06.16.
 */
public class GroceryStoresService implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private Context context;
    // An ID used to post the notification.
    public static final int NOTIFICATION_ID = 2;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    /**
     * Request code for location permission request.
     *
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean connectionFinished = false;


    public GroceryStoresService(Context cont) {
        context = cont;
        buildGoogleApiClient();
    }

    protected void start(Intent intent) {

        GroceryStoresDataSource groceryStoresDataSource = new GroceryStoresDataSource(context);
        groceryStoresDataSource.open();

        if (!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();

        // BEGIN_INCLUDE(service_onhandle)
        //Wait for GoogleApiClient connection
        while (!connectionFinished) {
            SystemClock.sleep(10);
        }
        //Check the grocery stores
        if (mLocation != null) {
            ArrayList<GroceryStore> stores = (ArrayList<GroceryStore>) groceryStoresDataSource.getAllGroceryStores();
            for (GroceryStore groceryStore : stores) {
                Location groceryStoreLocation = new Location(LocationManager.NETWORK_PROVIDER);
                groceryStoreLocation.setLatitude(groceryStore.getCoordinates().latitude);
                groceryStoreLocation.setLongitude(groceryStore.getCoordinates().longitude);
                float distance = mLocation.distanceTo(groceryStoreLocation);
                if (distance < 270) {
                    sendGroceryStoreNotification("You are near a Grocery Store!", groceryStore);
                }
            }
        }

        // Release the wake lock provided by the BroadcastReceiver.
        AlarmReceiver.completeWakefulIntent(intent);
        // END_INCLUDE(service_onhandle)
    }

    // Post a notification indicating whether a doodle was found.
    private void sendGroceryStoreNotification(String msg, GroceryStore groceryStore) {
        int requestID = (int) System.currentTimeMillis();
        Intent notificationIntent = new Intent(context, MapsActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        notificationIntent.putExtra("notification", true);
        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context, requestID,notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("You are near " + groceryStore.getName())
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);

        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(
                context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(new AppCompatActivity(), LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
            return;
        }
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        startLocationUpdate();
        connectionFinished = true;
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    private void startLocationUpdate() {
        initLocationRequest();

        if (ActivityCompat.checkSelfPermission(
                context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(new AppCompatActivity(), LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void stopLocationUpdate() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    private void initLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
    }
}