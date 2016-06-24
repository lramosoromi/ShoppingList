package com.mobilecomputing.shoppinglist;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends ListActivity implements ListView.OnItemClickListener {

    private ProductsListDataSource datasource;
    private String inputText = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocationManager locMan = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location loc = locMan.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(loc!=null){
            TextView text_lat = (TextView)findViewById(R.id.lat);
            TextView text_log = (TextView)findViewById(R.id.log);
            text_lat.setText(Double.toString(loc.getLatitude()));
            text_log.setText(Double.toString(loc.getLongitude()));
        }

        datasource = new ProductsListDataSource(this);
        datasource.open();

        List<ProductsList> values = datasource.getAllLists();

        // use the SimpleCursorAdapter to show the
        // elements in a ListView
        ArrayAdapter<ProductsList> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, values);
        setListAdapter(adapter);

        //* *EDIT* *
        ListView listview = (ListView) findViewById(android.R.id.list);
        listview.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> l, View v, int position, long id) {

        // Then you start a new Activity via Intent
        Intent intent = new Intent();
        intent.setClass(this, ScannerActivity.class);
        intent.putExtra("position", position);
        // Or / And
        intent.putExtra("id", id);
        startActivity(intent);
    }

    // Will be called via the onClick attribute
    // of the buttons in main.xml
    public void onClick(View view) {
        @SuppressWarnings("unchecked") final
        ArrayAdapter<ProductsList> adapter = (ArrayAdapter<ProductsList>) getListAdapter();
        final ProductsList[] productsList = new ProductsList[1];
        switch (view.getId()) {
            case R.id.add:
                //Creates popup for the list name
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Set the list name");

                // Set up the input
                final EditText input = new EditText(this);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        inputText = input.getText().toString();
                        // save the new list to the database
                        productsList[0] = datasource.createList(inputText);
                        adapter.add(productsList[0]);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
                break;
            case R.id.delete:
                if (getListAdapter().getCount() > 0) {
                    productsList[0] = (ProductsList) getListAdapter().getItem(0);
                    datasource.deleteList(productsList[0]);
                    adapter.remove(productsList[0]);
                }
                break;
            case R.id.gps:
                Intent intent_gps = new Intent();
                intent_gps.setClass(this, LocationTestActivity.class);
                startActivity(intent_gps);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        datasource.open();
        super.onResume();
    }

    @Override
    protected void onPause() {
        datasource.close();
        super.onPause();
    }
}
