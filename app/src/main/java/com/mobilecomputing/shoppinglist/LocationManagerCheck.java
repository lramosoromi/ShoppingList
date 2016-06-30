package com.mobilecomputing.shoppinglist;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;

/**
 * Class used to check and show a popup when using the Location in the map.
 * Created by rolithunderbird on 07.06.16.
 */
public class LocationManagerCheck {

    private LocationManager locationManager;
    private Boolean locationServiceBoolean = false;
    //Provider type of the Location
    public enum PROVIDERTYPE {
        NULL, GPS_PROVIDER, NETWORK_PROVIDER
    }
    private PROVIDERTYPE providerType = PROVIDERTYPE.NULL;
    private static AlertDialog alert;


    public LocationManagerCheck(Context context) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        updateLocationManagerCheck();
    }

    public Boolean isLocationServiceAvailable() {
        return locationServiceBoolean;
    }

    public PROVIDERTYPE getProviderType() {
        return providerType;
    }

    public PROVIDERTYPE getProviderTypeGPS() { return PROVIDERTYPE.GPS_PROVIDER; }

    public LocationManager getLocationManager() {
        return locationManager;
    }

    public void createLocationServiceError(final Activity activityObj, boolean isError) {

        String alertMessage;
        String alertTitle;
        if (isError) {
            alertMessage = "You need to activate location service to use this feature.\n" +
                    "        Please turn on network or GPS mode in location settings";
            alertTitle = "Caution";
        }
        else {
            alertMessage = "Currently you are using Wifi and GPRS location service.\n" +
                    "        For better accuracy please turn on GPS mode in location settings.";
            alertTitle = "Important";
        }
        // show alert dialog if Internet is not connected
        AlertDialog.Builder builder = new AlertDialog.Builder(activityObj);

        builder.setMessage(alertMessage)
                .setTitle(alertTitle)
                .setCancelable(false)
                .setPositiveButton(
                        "Settings",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(
                                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                activityObj.startActivity(intent);
                                alert.dismiss();
                            }
                        })
                .setNegativeButton(
                        "Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                alert.dismiss();
                            }
                        });
        alert = builder.create();
        alert.show();
    }

    public void updateLocationManagerCheck() {
        boolean gpsIsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkIsEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (networkIsEnabled && gpsIsEnabled) {
            locationServiceBoolean = true;
            providerType = PROVIDERTYPE.GPS_PROVIDER;

        } else if (!networkIsEnabled && gpsIsEnabled) {
            locationServiceBoolean = true;
            providerType = PROVIDERTYPE.GPS_PROVIDER;

        } else if (networkIsEnabled) {
            locationServiceBoolean = true;
            providerType = PROVIDERTYPE.NETWORK_PROVIDER;
        }
    }
}