package com.mobilecomputing.shoppinglist;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by rolithunderbird on 17.06.16.
 */
public class GroceryStore {
    private long id;
    private String name;
    private String address;
    private LatLng coordinates;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(LatLng coordinates) {
        this.coordinates = coordinates;
    }
}
