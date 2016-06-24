package com.mobilecomputing.shoppinglist;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;

import org.json.JSONException;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ScannerActivity extends ListActivity {

    private ProductsListDataSource datasource;
    private static final int ZBAR_SCANNER_REQUEST = 0;
    private long listId;
    private String inputTextName;
    private int inputTextPrice;
    private int inputTextCalories;
    private String inputTextOrganic;
    private Date inputTextExpire;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        datasource = new ProductsListDataSource(this);
        datasource.open();

        Intent intent = getIntent();
        int position = intent.getIntExtra("position", 10);
        List<ProductsList> lists = datasource.getAllLists();
        listId = lists.get(position).getId();

        List<Product> values = datasource.getAllProductsInList(listId);

        // use the SimpleCursorAdapter to show the
        // elements in a ListView
        ArrayAdapter<Product> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, values);
        setListAdapter(adapter);
    }

    public void launchScanner(View v) {
        if (isCameraAvailable()) {
            Intent intent = new Intent(this, ZBarScannerActivity.class);
            startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
        } else {
            Toast.makeText(this, "Rear Facing Camera Unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isCameraAvailable() {
        PackageManager pm = getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final ArrayAdapter<Product> adapter = (ArrayAdapter<Product>) getListAdapter();
        Product product;
        boolean isProduct;
        final long productId;

        switch (requestCode) {
            case ZBAR_SCANNER_REQUEST:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, "Scan Result = " +
                            data.getStringExtra(ZBarConstants.SCAN_RESULT), Toast.LENGTH_SHORT).show();

                    productId = Long.parseLong(data.getStringExtra(ZBarConstants.SCAN_RESULT));
                    product = datasource.searchProduct(productId);

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    LinearLayout layout = new LinearLayout(this);
                    layout.setOrientation(LinearLayout.VERTICAL);

                    builder.setTitle("Add product fields");

                    EditText inputName = null;
                    EditText inputPrice = null;
                    EditText inputCalories = null;
                    EditText inputOrganic = null;
                    final EditText inputExpires;
                    if (product == null) {
                        // Set up the input
                        inputName = new EditText(this);
                        inputPrice = new EditText(this);
                        inputCalories = new EditText(this);
                        inputOrganic = new EditText(this);
                        inputExpires = new EditText(this);
                        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                        inputName.setInputType(InputType.TYPE_CLASS_TEXT);
                        inputPrice.setInputType(InputType.TYPE_CLASS_NUMBER);
                        inputCalories.setInputType(InputType.TYPE_CLASS_NUMBER);
                        inputOrganic.setInputType(InputType.TYPE_CLASS_TEXT);
                        inputExpires.setInputType(InputType.TYPE_CLASS_DATETIME);

                        layout.addView(inputName);
                        layout.addView(inputPrice);
                        layout.addView(inputCalories);
                        layout.addView(inputOrganic);
                        layout.addView(inputExpires);

                        builder.setView(layout);
                        isProduct = false;
                    }
                    else {
                        inputExpires = new EditText(this);
                        inputExpires.setInputType(InputType.TYPE_CLASS_DATETIME);
                        builder.setView(inputExpires);

                        isProduct = true;
                    }

                    // Set up the buttons
                    final boolean finalIsProduct = isProduct;
                    final EditText finalInputName = inputName;
                    final EditText finalInputPrice = inputPrice;
                    final EditText finalInputCalories = inputCalories;
                    final EditText finalInputOrganic = inputOrganic;
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (!finalIsProduct) {

                                inputTextName = finalInputName.getText().toString();
                                inputTextPrice = Integer.parseInt(finalInputPrice.getText().toString());
                                inputTextCalories = Integer.parseInt(finalInputCalories.getText().toString());
                                inputTextOrganic = finalInputOrganic.getText().toString();
                                inputTextExpire = datasource.convertStringToDate(inputExpires.getText().toString());
                            }
                            else {
                                inputTextExpire = datasource.convertStringToDate(inputExpires.getText().toString());
                            }

                            Product finalProduct = null;
                            try {
                                finalProduct = datasource.addProductToList(listId, productId, inputTextName, inputTextPrice,
                                        inputTextCalories, inputTextOrganic, inputTextExpire);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            // save the new comment to the database
                            adapter.add(finalProduct);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();

                } else if(resultCode == RESULT_CANCELED && data != null) {
                    String error = data.getStringExtra(ZBarConstants.ERROR_INFO);
                    if(!TextUtils.isEmpty(error)) {
                        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    // Will be called via the onClick attribute
    // of the buttons in main.xml
    public void onClick(View view) {
        @SuppressWarnings("unchecked")
        ArrayAdapter<Product> adapter = (ArrayAdapter<Product>) getListAdapter();
        Product product;
        switch (view.getId()) {
            case R.id.delete1:
                if (getListAdapter().getCount() > 0) {
                    product = (Product) getListAdapter().getItem(0);
                    try {
                        datasource.deleteProductFromList(listId, product);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    adapter.remove(product);
                }
                break;
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
        //datasource.close();
        super.onPause();
    }

    private Product createPopupNewProduct(long id) {
        boolean isProduct;

        Product product = datasource.searchProduct(id);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set the list name");

        EditText inputName = null;
        EditText inputPrice = null;
        EditText inputCalories = null;
        EditText inputOrganic = null;
        final EditText inputExpires;
        if (product == null) {
            // Set up the input
            inputName = new EditText(this);
            inputPrice = new EditText(this);
            inputCalories = new EditText(this);
            inputOrganic = new EditText(this);
            inputExpires = new EditText(this);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            inputName.setInputType(InputType.TYPE_CLASS_TEXT);
            inputPrice.setInputType(InputType.TYPE_CLASS_NUMBER);
            inputCalories.setInputType(InputType.TYPE_CLASS_NUMBER);
            inputOrganic.setInputType(InputType.TYPE_CLASS_TEXT);
            inputExpires.setInputType(InputType.TYPE_CLASS_DATETIME);

            builder.setView(inputName);
            builder.setView(inputPrice);
            builder.setView(inputCalories);
            builder.setView(inputOrganic);
            builder.setView(inputExpires);

            isProduct = false;
        }
        else {
            inputExpires = new EditText(this);
            inputExpires.setInputType(InputType.TYPE_CLASS_DATETIME);
            builder.setView(inputExpires);

            isProduct = true;
        }

        // Set up the buttons
        final boolean finalIsProduct = isProduct;
        final EditText finalInputName = inputName;
        final EditText finalInputPrice = inputPrice;
        final EditText finalInputCalories = inputCalories;
        final EditText finalInputOrganic = inputOrganic;
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (finalIsProduct) {
                    inputTextName = finalInputName.getText().toString();
                    inputTextPrice = Integer.parseInt(finalInputPrice.getText().toString());
                    inputTextCalories = Integer.parseInt(finalInputCalories.getText().toString());
                    inputTextOrganic = finalInputOrganic.getText().toString();
                    inputTextExpire = datasource.convertStringToDate(inputExpires.getText().toString());
                }
                else {
                    inputTextExpire = datasource.convertStringToDate(inputExpires.getText().toString());
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

        try {
            return datasource.addProductToList(listId, id, inputTextName, inputTextPrice,
                    inputTextCalories, inputTextOrganic, inputTextExpire);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}