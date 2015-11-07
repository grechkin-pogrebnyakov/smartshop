package com.technopark.smartbiz.buisnessLogic.addProduct;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.technopark.smartbiz.R;
import com.technopark.smartbiz.database.SmartShopContentProvider;

public class AddProductActivity extends AppCompatActivity {

    String name, priceCostProduct, priceSellingProduct, count, barcode, description, photoPath;

    ImageButton addProductPhotoButton;
    Button addProductButton;
    Button scanBarcodeButton;

    EditText nameEditText, priceCostProductEditText,priceSellingProductEditText,
    barcodeEditText, countEditText, descriptionEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        photoPath = new String();

        initializationEditTextFields();
        initializationButtons();

        initializationButtonsListener();

    }

    private void initializationEditTextFields() {
        nameEditText = (EditText) findViewById(R.id.content_add_product_name_textField);
        priceCostProductEditText = (EditText) findViewById(R.id.content_add_product_price_cost_textField);
        priceSellingProductEditText = (EditText) findViewById(R.id.content_add_product_price_selling);
        barcodeEditText = (EditText) findViewById(R.id.content_add_product_barcode);
        countEditText = (EditText) findViewById(R.id.content_add_product_count);
        descriptionEditText = (EditText) findViewById(R.id.content_add_product_description);
    }

    private void initializationButtons () {
        addProductPhotoButton = (ImageButton) findViewById(R.id.content_add_product_photo);
        addProductButton = (Button) findViewById(R.id.content_add_product_button_add_product);
        scanBarcodeButton = (Button) findViewById(R.id.content_add_product_scan_barcode);
    }

    private void initializationButtonsListener () {
        addProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionForAddProductButton();
            }
        });

        scanBarcodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator integrator = new IntentIntegrator(AddProductActivity.this);
                integrator.initiateScan(IntentIntegrator.PRODUCT_CODE_TYPES);
            }
        });
    }

    private void actionForAddProductButton () {
        name = nameEditText.getText().toString();
        priceCostProduct = priceCostProductEditText.getText().toString();
        priceSellingProduct = priceSellingProductEditText.getText().toString();
        count = countEditText.getText().toString();
        barcode = barcodeEditText.getText().toString();
        description = descriptionEditText.getText().toString();

        if (addRecord(name, priceCostProduct, priceSellingProduct, count, barcode, description,
                photoPath) != null) {
            Toast.makeText(getApplicationContext(), "Продукт добавлен", Toast.LENGTH_LONG);
        } Toast.makeText(getApplicationContext(), "Ошибка добавления продукта", Toast.LENGTH_LONG);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (result != null) {
            String contents = result.getContents();
            if (contents != null) {
                Toast.makeText(getApplicationContext(), "Успешно отсканированно!" + result.toString(), Toast.LENGTH_LONG).show();
                barcodeEditText.setText( result.getContents() );
            } else {
                Toast.makeText(getApplicationContext(), "failed scan", Toast.LENGTH_LONG).show();
            }
        }
    }

    private Uri addRecord(String name, String priceCostProduct, String priceSellingProduct,
                          String count, String barcode, String description, String photoPath ) {
        // Defines a new Uri object that receives the result of the insertion
        Uri mNewUri;

        // Defines an object to contain the new values to insert
        ContentValues mNewValues = new ContentValues();

        /*
        * Sets the values of each column and inserts the word. The arguments to the "put"
        * method are "column name" and "value"
        */
        mNewValues.put("barcode", barcode);
        mNewValues.put("photo_path", photoPath);
        mNewValues.put("name", name);
        mNewValues.put("price_selling_product", priceSellingProduct);
        mNewValues.put("price_cost_product", priceCostProduct);
        mNewValues.put("description", description);
        mNewValues.put("count", count);

        mNewUri = getContentResolver().insert(
                SmartShopContentProvider.ITEMS_CONTENT_URI,   // the user dictionary content URI
                mNewValues                          // the values to insert
        );
        return mNewUri;
    }

}
