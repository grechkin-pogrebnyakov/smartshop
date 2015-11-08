package com.technopark.smartbiz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.technopark.smartbiz.buisnessLogic.addProduct.AddProductActivity;
import com.technopark.smartbiz.userIdentification.LoginActivity;


public class MainActivity extends AppCompatActivity {

	SharedPreferences sharedPreferences;
	public static final String APP_PREFERENCES = "mysettings";
	public static final String SESSION_ID = "sesion_id";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		sharedPreferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		LineChart mainChart = (LineChart) findViewById(R.id.content_main_chart);


		Button logOut = (Button) findViewById(R.id.button_logout);

		logOut.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				sharedPreferences.edit().remove(SESSION_ID).commit();
				startActivity(new Intent(getApplicationContext(), LoginActivity.class));
			}
		});

		Button goToAddProduct = (Button) findViewById(R.id.go_to_add_product);
		goToAddProduct.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent goAddProduct = new Intent(getApplicationContext(), AddProductActivity.class);
				startActivity(goAddProduct);
			}
		});

		Button scanBarcode = (Button) findViewById(R.id.scan_button);

		scanBarcode.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
				integrator.initiateScan(IntentIntegrator.PRODUCT_CODE_TYPES);
			}
		});

		Button purchaseButton = (Button) findViewById(R.id.content_main_button_purchase);
		purchaseButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, PurchaseActivity.class);
				startActivity(intent);
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		if (result != null) {
			String contents = result.getContents();
			if (contents != null) {
				Toast.makeText(getApplicationContext(), "Sucssed scan" + result.toString(), Toast.LENGTH_LONG).show();
			}
			else {
				Toast.makeText(getApplicationContext(), "failed scan", Toast.LENGTH_LONG).show();
			}
		}
	}

}
