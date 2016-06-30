package com.mobilecomputing.shoppinglist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

public class MyListActivity extends ListActivity implements ListView.OnItemClickListener{

    private ProductsListDataSource datasource;
    private String inputText = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

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
    public void onItemClick(AdapterView<?> l, View v, final int position, final long id) {
        @SuppressWarnings("unchecked") final
        ArrayAdapter<ProductsList> adapter = (ArrayAdapter<ProductsList>) getListAdapter();

        //Creates popup for the list posibilities
        final AlertDialog alert = new AlertDialog.Builder(this).create();

        List<ProductsList> lists = datasource.getAllLists();
        String listName = lists.get(position).getName();
        alert.setTitle("List " + listName);

        // Set the button to edit
        alert.setButton(AlertDialog.BUTTON_POSITIVE, "Edit List", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Then you start a new Activity via Intent
                Intent intent = new Intent();
                intent.setClass(MyListActivity.this, ScannerActivity.class);
                intent.putExtra("position", position);
                // Or / And
                intent.putExtra("id", id);
                startActivity(intent);
            }
        });

        // Set the button to delete
        alert.setButton(AlertDialog.BUTTON_NEGATIVE, "Delete List", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                adapter.remove(adapter.getItem(position));
                datasource.deleteList(datasource.getAllLists().get(position));
                Toast.makeText(MyListActivity.this,
                        "List was deleted",
                        Toast.LENGTH_SHORT).show();
            }
        });
        alert.show();
        adapter.notifyDataSetChanged();
    }

    // Will be called via the onClick attribute
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
            default:
                break;
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
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
