package com.technopark.smartbiz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.technopark.smartbiz.buisnessLogic.addProduct.AddProductActivity;
import com.technopark.smartbiz.database.DatabaseHelper;
import com.technopark.smartbiz.database.SmartShopContentProvider;
import com.technopark.smartbiz.screnListView.ListAddedProducts;
import com.technopark.smartbiz.userIdentification.LoginActivity;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

	private SharedPreferences sharedPreferences;

	public static final String APP_PREFERENCES = "mysettings";
	public static final String TOKEN_AUTORIZATION = "token";

	private CheckContentObserver checkContentObserver = new CheckContentObserver();
	private DatabaseHelper dbHelper;

	private LineChart mainChart;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		sharedPreferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

		getContentResolver()
				.registerContentObserver(SmartShopContentProvider.CHECKS_CONTENT_URI, false, checkContentObserver);
		dbHelper = new DatabaseHelper(this);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		mainChart = (LineChart) findViewById(R.id.content_main_chart);
		setupChart();
		fillChart();

		Button logOut = (Button) findViewById(R.id.button_logout);
		logOut.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				sharedPreferences.edit().remove(TOKEN_AUTORIZATION).commit();
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
				Intent intent = new Intent(MainActivity.this, CheckActivity.class);
				startActivity(intent);
			}
		});

		Button showProductsButton = (Button) findViewById(R.id.content_main_button_show_products);
		showProductsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent show = new Intent(getApplicationContext(), ListAddedProducts.class);
				startActivity(show);
			}
		});
	}

	private void setupChart() {
		mainChart.setDescription("");

		XAxis xAxis = mainChart.getXAxis();
		xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
	}

	private void fillChart() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		String[] columns = new String[]{
				"strftime('%d-%m-%Y', date_time) as date",
				"sum(price_selling_product * count) as total_price"
		};

		Cursor cursor = db
				.query(DatabaseHelper.CHECKS_TABLE_NAME, columns, null, null, "date", null, null);

		ArrayList<Entry> entries = new ArrayList<>();
		ArrayList<String> labels = new ArrayList<>();

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				int setSize = cursor.getCount();
				for (int i = 0; i < setSize; ++i) {
					entries.add(new Entry(cursor.getInt(cursor.getColumnIndex("total_price")), i));
					labels.add(cursor.getString(cursor.getColumnIndex("date")));
					cursor.moveToNext();
				}
			}
			cursor.close();
		}
		dbHelper.close();

		LineDataSet dataSet = new LineDataSet(entries, "Оборот");
		LineData lineData = new LineData(labels, dataSet);

		mainChart.setData(lineData);
		mainChart.invalidate();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		getContentResolver().unregisterContentObserver(checkContentObserver);
	}

	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		if (result != null) {
			String contents = result.getContents();
			if (contents != null) {
				Toast.makeText(getApplicationContext(), "Sucssed scan" + result.toString(),
						Toast.LENGTH_LONG).show();
			}
			else {
				Toast.makeText(getApplicationContext(), "failed scan", Toast.LENGTH_LONG).show();
			}
		}
	}

	private class CheckContentObserver extends ContentObserver {

		private String CHECK_CONTENT_OBSERVER_LOG = "CheckContentObserver";

		public CheckContentObserver() {
			super(null);
		}

		@Override
		public boolean deliverSelfNotifications() {
			return false;
		}

		@Override
		public void onChange(boolean selfChange) {
			Log.d(CHECK_CONTENT_OBSERVER_LOG, "onChange");
		}

		@Override
		public void onChange(boolean selfChange, Uri uri) {
			Log.d(CHECK_CONTENT_OBSERVER_LOG, "onChangeUri");
		}
	}
}
