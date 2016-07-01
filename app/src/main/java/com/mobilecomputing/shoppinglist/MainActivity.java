package com.mobilecomputing.shoppinglist;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;

import org.json.JSONException;

public class MainActivity extends AppCompatActivity {

    private ProductsListDataSource datasource;
    private GroceryStoresDataSource groceryStoresDataSource;
    private static String INVENTORY_NAME = "INVENTORY";
    private static final int ZBAR_SCANNER_REQUEST = 0;
    private boolean notificationBack;
    private AlarmReceiver alarm = new AlarmReceiver();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        notificationBack = intent.getBooleanExtra("notificationBack", false);

        datasource = new ProductsListDataSource(this);
        datasource.open();

        groceryStoresDataSource = new GroceryStoresDataSource(this);
        groceryStoresDataSource.open();

        // With this I check if its the first time I use the app
        if (!isInventoryCreated()) {
            createInventory();
            addGroceryStores();
        }

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
            case R.id.button_map:
                //start gps
                Intent intent1 = new Intent();
                intent1.setClass(MainActivity.this, MapsActivity.class);
                intent1.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
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

    @Override
    public void onBackPressed() {
        finish();
    }

    private boolean isInventoryCreated() {
        return datasource.isInventoryCreated();
    }

    private void createInventory() {
        datasource.createList(INVENTORY_NAME);
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

    private void addGroceryStores() {
        groceryStoresDataSource.createGroceryStore(
                "Example Grocery Store",
                "Pestalozzistraße 62, 72762 Reutlingen, Germany",
                "48.482964, 9.187570");
        groceryStoresDataSource.createGroceryStore(
                "PENNY Markt Filiale",
                "Pestalozzistraße 5, 72762 Reutlingen, Germany",
                "48.484860, 9.194667");
        groceryStoresDataSource.createGroceryStore(
                "Edeka Möck",
                "Friedrich-Naumann-Straße 36, 72762 Reutlingen, Germany",
                "48.487295, 9.190938");
        groceryStoresDataSource.createGroceryStore(
                "Edeka",
                "An der Kreuzeiche 23, 72762 Reutlingen, Germany",
                "48.479022, 9.199114");
        groceryStoresDataSource.createGroceryStore(
                "Aldi Süd",
                "Ringelbachstraße 183, 72762 Reutlingen, Germany",
                "48.477935, 9.200150");
    }
}
