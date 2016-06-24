package com.mobilecomputing.shoppinglist;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;

import org.json.JSONException;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ProductsListDataSource datasource;
    private static String INVENTROY_NAME = "INVENTORY";
    private static final int ZBAR_SCANNER_REQUEST = 0;
    private AlarmReceiver alarm = new AlarmReceiver();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        datasource = new ProductsListDataSource(this);
        datasource.open();

        if (!isInventoryCreated())
            createInventory();

        //Create the alarm for the ExpirationDateService
        alarm.setAlarm(this);
    }

    // Will be called via the onClick attribute
    // of the buttons in main.xml
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.button_lists:
                Intent intent = new Intent();
                intent.setClass(this, MyListActivity.class);
                startActivity(intent);
                break;
            case R.id.delete:
                ProductsList inventory = datasource.getInventory();
                if (inventory.getProducts().size() == 0) {
                    Toast.makeText(this, "There are no products in the inventory", Toast.LENGTH_SHORT)
                            .show();
                }
                else {
                    AlertDialog alertDialog = new AlertDialog.Builder(this).create();

                    alertDialog.setTitle("Select way to delete");

                    // Set the button to scan
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Scanner",
                            new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            launchScanner(view);
                        }
                    });

                    // Set the button to write the barcode
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Barcode",
                            new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Create new popup to insert the barcode
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            LinearLayout layout = new LinearLayout(MainActivity.this);
                            layout.setOrientation(LinearLayout.VERTICAL);

                            builder.setTitle("Insert product barcode");
                            final EditText editText = new EditText(MainActivity.this);
                            editText.setHint("Barcode");
                            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                            layout.addView(editText);
                            builder.setView(layout);

                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    String barcode = editText.getText().toString();
                                    long productId = Long.parseLong(barcode);
                                    Product product = datasource.searchProduct(productId);
                                    long inventoryListId = datasource.getInventoryListId();
                                    if (product != null) {
                                        try {
                                            datasource.deleteProductFromList(inventoryListId, product);
                                            Toast.makeText(MainActivity.this,
                                                    "The product = " + barcode + " was deleted from the inventory",
                                                    Toast.LENGTH_SHORT).show();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    else {
                                        Toast.makeText(MainActivity.this,
                                                "The product " + barcode + " is not in the inventory",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            builder.show();
                        }
                    });
                    alertDialog.show();
                }
                break;
            case R.id.button_gps:
                //start gps
                Intent intent1 = new Intent();
                intent1.setClass(MainActivity.this, GPSActivity.class);
                startActivity(intent1);
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Product product;
        final long productId;

        switch (requestCode) {
            case ZBAR_SCANNER_REQUEST:
                if (resultCode == RESULT_OK) {
                    productId = Long.parseLong(data.getStringExtra(ZBarConstants.SCAN_RESULT));
                    product = datasource.searchProduct(productId);
                    long inventoryListId = datasource.getInventoryListId();
                    if (product != null) {
                        try {
                            datasource.deleteProductFromList(inventoryListId, product);
                            Toast.makeText(MainActivity.this,
                                    "The product = " + productId + " was deleted from the inventory",
                                    Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        Toast.makeText(MainActivity.this,
                                "The product = " + productId + " is not in the inventory",
                                Toast.LENGTH_SHORT).show();
                    }
                } else if(resultCode == RESULT_CANCELED && data != null) {
                    String error = data.getStringExtra(ZBarConstants.ERROR_INFO);
                    if(!TextUtils.isEmpty(error)) {
                        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
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

    private boolean isInventoryCreated() {
        List<ProductsList> lists = datasource.getAllLists();
        for (ProductsList prodList : lists) {
            String listName = prodList.getName();
            if (listName.equals(INVENTROY_NAME))
                return true;
        }
        return false;
    }

    private void createInventory() {
        datasource.createList(INVENTROY_NAME);
    }

    private void launchScanner(View v) {
        if (isCameraAvailable()) {
            Intent intent = new Intent(this, ZBarScannerActivity.class);
            startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
        } else {
            Toast.makeText(this, "Rear Facing Camera Unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isCameraAvailable() {
        PackageManager pm = getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

}
