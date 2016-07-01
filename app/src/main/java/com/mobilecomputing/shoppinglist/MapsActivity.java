package com.mobilecomputing.shoppinglist;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private GoogleMap mMap;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;

    /**
     * Request code for location permission request.
     *
     */
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    public GroceryStoresDataSource dataSource;
    LocationManagerCheck locationManagerCheck;
    private boolean isNotificationActivity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Intent intent = getIntent();
        boolean notificationBool = intent.getBooleanExtra("notification", false);

        if (notificationBool) {
            isNotificationActivity = true;
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        dataSource = new GroceryStoresDataSource(this);
        dataSource.open();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        //Esto es para cambiar donde arranca el mapa
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition(new LatLng(48.481905, 9.188492), 16, 0, 0)));

        for (GroceryStore groceryStore : dataSource.getAllGroceryStores()) {
            map.addMarker(new MarkerOptions()
                    .position(groceryStore.getCoordinates())
                    .title(groceryStore.getName())
                    .snippet(groceryStore.getAddress().split(",")[0]));
        }

        // AGREGADO POR MI para poder ver mi location en el mapa
        mMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();
    }

    //A PARTIR DE ACA ES LO NECESARIO PARA PODER VER MI LOCATION EN EL MAPA

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        //  Initialize the locationManagerCheck
        locationManagerCheck = new LocationManagerCheck(this);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {

        //Popup que me dice de habilitar el GPS
        locationManagerCheck.updateLocationManagerCheck();

        if (locationManagerCheck.isLocationServiceAvailable()) {
            if (locationManagerCheck.getProviderType() != locationManagerCheck.getProviderTypeGPS()) {
                locationManagerCheck.createLocationServiceError(MapsActivity.this, false);
            }
        } else {
            locationManagerCheck.createLocationServiceError(MapsActivity.this, true);
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        finish();
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }
}