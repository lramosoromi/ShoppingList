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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;

import org.json.JSONException;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ScannerActivity extends ListActivity implements ListView.OnItemClickListener {

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

        //* *EDIT* *
        ListView listview = (ListView) findViewById(android.R.id.list);
        listview.setOnItemClickListener(this);
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

                    EditText inputName = null;
                    EditText inputPrice = null;
                    EditText inputCalories = null;
                    EditText inputOrganic = null;
                    final EditText inputExpires;
                    if (product == null) {
                        builder.setTitle("Add product fields");

                        // Set up the input
                        inputName = new EditText(this);
                        inputName.setHint("Name");
                        inputPrice = new EditText(this);
                        inputPrice.setHint("Price");
                        inputCalories = new EditText(this);
                        inputCalories.setHint("Calories");
                        inputOrganic = new EditText(this);
                        inputOrganic.setHint("Organic (write true or false)");
                        inputExpires = new EditText(this);
                        inputExpires.setHint("Date of expiry (format dd/mm/yyyy)");

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
                        builder.setTitle("Product already exists");

                        inputExpires = new EditText(this);
                        inputExpires.setHint("Date of expiry (format dd/mm/yyyy)");
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
                            // Add the product to the list
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
    public void onClick(final View view) {
        @SuppressWarnings("unchecked") final
        ArrayAdapter<Product> adapter = (ArrayAdapter<Product>) getListAdapter();
        switch (view.getId()) {
            case R.id.add:
                if (getListAdapter().getCount() > 0) {
                    try {
                        datasource.addListToInventory(listId);
                        Toast.makeText(this,
                                "All the products in the list have been added to the inventory",
                                Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        intent.setClass(this, MyListActivity.class);
                        startActivity(intent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.scan_btn:
                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Select way to add product");

                // Set scan button
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Scanner",
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        launchScanner(view);
                    }
                });

                // Set button to write the barcode manually
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Barcode", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ScannerActivity.this);
                        LinearLayout layout = new LinearLayout(ScannerActivity.this);
                        layout.setOrientation(LinearLayout.VERTICAL);

                        builder.setTitle("Insert product barcode");
                        final EditText editText = new EditText(ScannerActivity.this);
                        editText.setHint("Barcode");
                        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                        layout.addView(editText);

                        builder.setView(layout);

                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                final String barcode = editText.getText().toString();
                                final long productId = Long.parseLong(barcode);
                                boolean isProduct;

                                //region Add product dialog (same as above)
                                Product product = datasource.searchProduct(productId);

                                AlertDialog.Builder builder2 = new AlertDialog.Builder(ScannerActivity.this);
                                LinearLayout layout2 = new LinearLayout(ScannerActivity.this);
                                layout2.setOrientation(LinearLayout.VERTICAL);

                                EditText inputName = null;
                                EditText inputPrice = null;
                                EditText inputCalories = null;
                                EditText inputOrganic = null;
                                final EditText inputExpires;
                                if (product == null) {
                                    builder2.setTitle("Add product fields");

                                    // Set up the input
                                    inputName = new EditText(ScannerActivity.this);
                                    inputName.setHint("Name");
                                    inputPrice = new EditText(ScannerActivity.this);
                                    inputPrice.setHint("Price");
                                    inputCalories = new EditText(ScannerActivity.this);
                                    inputCalories.setHint("Calories");
                                    inputOrganic = new EditText(ScannerActivity.this);
                                    inputOrganic.setHint("Organic (write true or false)");
                                    inputExpires = new EditText(ScannerActivity.this);
                                    inputExpires.setHint("Date of expiry (format dd/mm/yyyy)");

                                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                                    inputName.setInputType(InputType.TYPE_CLASS_TEXT);
                                    inputPrice.setInputType(InputType.TYPE_CLASS_NUMBER);
                                    inputCalories.setInputType(InputType.TYPE_CLASS_NUMBER);
                                    inputOrganic.setInputType(InputType.TYPE_CLASS_TEXT);
                                    inputExpires.setInputType(InputType.TYPE_CLASS_DATETIME);

                                    layout2.addView(inputName);
                                    layout2.addView(inputPrice);
                                    layout2.addView(inputCalories);
                                    layout2.addView(inputOrganic);
                                    layout2.addView(inputExpires);

                                    builder2.setView(layout2);
                                    isProduct = false;
                                } else {
                                    builder2.setTitle("Product already exists");

                                    inputExpires = new EditText(ScannerActivity.this);
                                    inputExpires.setHint("Date of expiry (format dd/mm/yyyy)");
                                    inputExpires.setInputType(InputType.TYPE_CLASS_DATETIME);
                                    builder2.setView(inputExpires);

                                    isProduct = true;
                                }

                                // Set up the buttons
                                final boolean finalIsProduct = isProduct;
                                final EditText finalInputName = inputName;
                                final EditText finalInputPrice = inputPrice;
                                final EditText finalInputCalories = inputCalories;
                                final EditText finalInputOrganic = inputOrganic;
                                builder2.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (!finalIsProduct) {

                                            inputTextName = finalInputName.getText().toString();
                                            inputTextPrice = Integer.parseInt(finalInputPrice.getText().toString());
                                            inputTextCalories = Integer.parseInt(finalInputCalories.getText().toString());
                                            inputTextOrganic = finalInputOrganic.getText().toString();
                                            inputTextExpire = datasource.convertStringToDate(inputExpires.getText().toString());
                                        } else {
                                            inputTextExpire = datasource.convertStringToDate(inputExpires.getText().toString());
                                        }

                                        Product finalProduct = null;
                                        try {
                                            finalProduct = datasource.addProductToList(listId, productId, inputTextName, inputTextPrice,
                                                    inputTextCalories, inputTextOrganic, inputTextExpire);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        // Add the product to the list
                                        adapter.add(finalProduct);
                                    }
                                });
                                builder2.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                                builder2.show();
                                //endregion
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                        builder.show();
                    }
                });
                alertDialog.show();
                break;
            default:
                break;
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> l, View v, final int position, final long id) {
        @SuppressWarnings("unchecked") final
        ArrayAdapter<Product> adapter = (ArrayAdapter<Product>) getListAdapter();

        List<Product> products = datasource.getAllProductsInList(listId);
        final Product product = products.get(position);

        // Creates popup for the product posibilities
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Delete product");

        alertDialog.setMessage("You are about to delete the product " + product.getName());

        // Set positive button
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    datasource.deleteProductFromList(listId, product);
                    Toast.makeText(ScannerActivity.this, "Product " + product.getName() + " deleted",
                            Toast.LENGTH_SHORT).show();
                    adapter.remove(adapter.getItem(position));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        // Set negative button
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        alertDialog.show();
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
}