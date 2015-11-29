package com.technopark.smartbiz;

import android.content.ContentValues;
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
import com.technopark.smartbiz.api.HttpsHelper;
import com.technopark.smartbiz.api.SmartShopUrl;
import com.technopark.smartbiz.businessLogic.addProduct.AddProductActivity;
import com.technopark.smartbiz.businessLogic.discard.DiscardActivity;
import com.technopark.smartbiz.businessLogic.employees.EmployeeListActivity;
import com.technopark.smartbiz.businessLogic.employees.EmployeeRegistrationActivity;
import com.technopark.smartbiz.businessLogic.productSales.CheckActivity;
import com.technopark.smartbiz.businessLogic.shopProfile.ShopProfileActivity;
import com.technopark.smartbiz.businessLogic.showProducts.ListAddedProducts;
import com.technopark.smartbiz.businessLogic.supply.SupplyActivity;
import com.technopark.smartbiz.businessLogic.userIdentification.LoginActivity;
import com.technopark.smartbiz.businessLogic.userIdentification.UserIdentificationContract;
import com.technopark.smartbiz.database.DatabaseHelper;
import com.technopark.smartbiz.database.SmartShopContentProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.technopark.smartbiz.Utils.isResponseSuccess;


public class MainActivity extends AppCompatActivity {

	public static final String APP_PREFERENCES = "mysettings";

	private SharedPreferences sharedPreferences;
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
				sharedPreferences.edit().remove(UserIdentificationContract.STATUS_AUTHORIZATION_KEY).apply();
				sharedPreferences.edit().remove(UserIdentificationContract.TOKEN_AUTHORIZATION).apply();
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

		Button editShopProfileButton = (Button) findViewById(R.id.content_main_button_edit_shop_profile);
		editShopProfileButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent show = new Intent(getApplicationContext(), ShopProfileActivity.class);
				show.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				startActivity(show);
			}
		});

		Button supplyButton = (Button) findViewById(R.id.content_main_button_supply);
		supplyButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), SupplyActivity.class);
				startActivity(intent);
			}
		});

		Button discardButton = (Button) findViewById(R.id.content_main_button_discarding);
		discardButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), DiscardActivity.class);
				startActivity(intent);
			}
		});

		Button employeesButton = (Button) findViewById(R.id.content_main_button_employees);
		employeesButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), EmployeeListActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				startActivity(intent);
			}
		});

		Button employeeRegistrationButton = (Button) findViewById(R.id.content_main_button_employee_registration);
		employeeRegistrationButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), EmployeeRegistrationActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				startActivity(intent);
			}
		});

		Button syncButton = (Button) findViewById(R.id.content_main_button_sync);
		syncButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new HttpsHelper.HttpsAsyncTask(SmartShopUrl.Shop.Item.URL_ITEM_LIST, null, productCallback, getApplicationContext())
						.execute(HttpsHelper.Method.GET);
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
		db.close();

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

	private HttpsHelper.HttpsAsyncTask.HttpsAsyncTaskCallback productCallback = new HttpsHelper.HttpsAsyncTask.HttpsAsyncTaskCallback() {
		@Override
		public void onPreExecute() {}

		@Override
		public void onPostExecute(JSONObject jsonObject) {
			try {
				if (isResponseSuccess(jsonObject.getInt(HttpsHelper.RESPONSE_CODE))) {
					DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());

					databaseHelper.dropTable(DatabaseHelper.PRODUCTS_TABLE_NAME);

					JSONArray products = jsonObject.getJSONArray(HttpsHelper.RESPONSE);
					for (int i = 0; i < products.length(); ++i) {
						JSONObject product = products.getJSONObject(i);

						String productName = product.getString("productName");
						String descriptionProduct = product.getString("descriptionProduct");
						String priceSellingProduct = product.getString("priceSellingProduct");
						String pricePurchaseProduct = product.getString("pricePurchaseProduct");
						String productBarcode = product.getString("productBarcode");
						String count = product.getString("count");
						String id = product.getString("id");

						ContentValues contentValues = new ContentValues();

						contentValues.put("name", productName);
						contentValues.put("description", descriptionProduct);
						contentValues.put("price_selling_product", priceSellingProduct);
						contentValues.put("price_cost_product", pricePurchaseProduct);
						contentValues.put("barcode", productBarcode);
						contentValues.put("count", count);
						contentValues.put("_id", id);

						getContentResolver().insert(SmartShopContentProvider.PRODUCTS_CONTENT_URI, contentValues);
					}

					Toast.makeText(getApplicationContext(), "Данные успешно синхронизированы!", Toast.LENGTH_LONG)
							.show();
				}
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
		}
	};
}
