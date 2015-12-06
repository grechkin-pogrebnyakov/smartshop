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
import com.technopark.smartbiz.businessLogic.changesPriceList.ListChangesPriceActivity;
import com.technopark.smartbiz.businessLogic.discard.DiscardActivity;
import com.technopark.smartbiz.businessLogic.employees.EmployeeListActivity;
import com.technopark.smartbiz.businessLogic.employees.EmployeeRegistrationActivity;
import com.technopark.smartbiz.businessLogic.productSales.CheckActivity;
import com.technopark.smartbiz.businessLogic.shopProfile.ShopProfileActivity;
import com.technopark.smartbiz.businessLogic.showProducts.ListAddedProducts;
import com.technopark.smartbiz.businessLogic.supply.SupplyActivity;
import com.technopark.smartbiz.businessLogic.userIdentification.AccessControl;
import com.technopark.smartbiz.businessLogic.userIdentification.InteractionWithUI;
import com.technopark.smartbiz.businessLogic.userIdentification.UserIdentificationContract;
import com.technopark.smartbiz.businessLogic.userIdentification.activities.LoginActivity;
import com.technopark.smartbiz.database.ContractClass;
import com.technopark.smartbiz.database.DatabaseHelper;
import com.technopark.smartbiz.database.SmartShopContentProvider;
import com.technopark.smartbiz.gcm.RegistrationIntentService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.technopark.smartbiz.Utils.isResponseSuccess;


public class MainActivity extends AppCompatActivity implements InteractionWithUI {

	public static final String APP_PREFERENCES = "mysettings";

	private SharedPreferences sharedPreferences;
	private CheckContentObserver checkContentObserver = new CheckContentObserver();
	private DatabaseHelper dbHelper;

	private AccessControl accessControl;
	private LineChart mainChart;
	private Button logOut, purchaseButton, showProductsButton, discardButton,
			goToAddProduct, editShopProfileButton, supplyButton, employeesButton, employeeRegistrationButton;

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

		accessControl = new AccessControl(getApplicationContext(), this, UserIdentificationContract.REQUEST_CODE_ACCESS_LOGIN);
		accessControl.displayActivityOfAccessRights();

		// Start IntentService to register this application with GCM.
		Intent intent = new Intent(this, RegistrationIntentService.class);
		startService(intent);
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
				"sum(price_selling_product * _count) as total_price"
		};

		Cursor cursor = db
				.query(ContractClass.Сhecks.TABLE_NAME, columns, null, null, ContractClass.Сhecks.DATE_TIME, null, null);

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

	@Override
	public void netActionResponse(int requestActionCode, JSONObject jsonResponce) {

	}

	@Override
	public void callbackAccessControl(int requestActionCode, String accessRightIdentificator) {
		switch (requestActionCode) {
			case UserIdentificationContract.REQUEST_CODE_ACCESS_LOGIN:
				initializationActivitiByStatus(accessRightIdentificator);
				break;
		}
	}

	@Override
	public void showToast(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	}

	private void initializationActivitiByStatus(String accessRightIdentificator) {
		initializationActivitiElementsForOwner();
		switch (accessRightIdentificator) {
			case UserIdentificationContract.SUCCESS_AUTHORIZATION_EMPLOYEE:
				retainElementsForEmployee();
				break;
		}
	}

	private void initializationActivitiElementsForOwner() {
		// общие элементы
		logOut = (Button) findViewById(R.id.button_logout);
		logOut.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				sharedPreferences.edit().remove(UserIdentificationContract.STATUS_AUTHORIZATION_KEY).apply();
				sharedPreferences.edit().remove(UserIdentificationContract.TOKEN_AUTHORIZATION).apply();
				startActivity(new Intent(getApplicationContext(), LoginActivity.class));
			}
		});

		purchaseButton = (Button) findViewById(R.id.content_main_button_purchase);
		purchaseButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, CheckActivity.class);
				startActivity(intent);
			}
		});

		showProductsButton = (Button) findViewById(R.id.content_main_button_show_products);
		showProductsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent show = new Intent(getApplicationContext(), ListAddedProducts.class);
				startActivity(show);
			}
		});

		discardButton = (Button) findViewById(R.id.content_main_button_discarding);
		discardButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), DiscardActivity.class);
				startActivity(intent);
			}
		});

		//элементы для владельца
		mainChart = (LineChart) findViewById(R.id.content_main_chart);
		setupChart();
		fillChart();

		goToAddProduct = (Button) findViewById(R.id.go_to_add_product);
		goToAddProduct.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent goAddProduct = new Intent(getApplicationContext(), AddProductActivity.class);
				startActivity(goAddProduct);
			}
		});


		editShopProfileButton = (Button) findViewById(R.id.content_main_button_edit_shop_profile);
		editShopProfileButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent show = new Intent(getApplicationContext(), ShopProfileActivity.class);
				show.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				startActivity(show);
			}
		});

		supplyButton = (Button) findViewById(R.id.content_main_button_supply);
		supplyButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), SupplyActivity.class);
				startActivity(intent);
			}
		});

		employeesButton = (Button) findViewById(R.id.content_main_button_employees);
		employeesButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), EmployeeListActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				startActivity(intent);
			}
		});

		employeeRegistrationButton = (Button) findViewById(R.id.content_main_button_employee_registration);
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

				// TODO Calculate time
				new HttpsHelper.HttpsAsyncTask(SmartShopUrl.Shop.Check.URL_CHECK_LIST + "?time1=12099242&time2=12209039393&type=0", null, checkCallback, getApplicationContext())
						.execute(HttpsHelper.Method.GET);
			}
		});

		Button priceUpdate = (Button) findViewById(R.id.content_main_button_price_update);
		priceUpdate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), ListChangesPriceActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				startActivity(intent);
			}
		});
	}

	private void retainElementsForEmployee() {
		mainChart.setVisibility(View.GONE);
		goToAddProduct.setVisibility(View.GONE);
		editShopProfileButton.setVisibility(View.GONE);
		supplyButton.setVisibility(View.GONE);
		employeesButton.setVisibility(View.GONE);
		employeeRegistrationButton.setVisibility(View.GONE);
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

					databaseHelper.dropTable(ContractClass.Products.TABLE_NAME);

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

						contentValues.put(ContractClass.Products.NAME, productName);
						contentValues.put(ContractClass.Products.DESCRIPTION, descriptionProduct);
						contentValues.put(ContractClass.Products.PRICE_SELLING, priceSellingProduct);
						contentValues.put(ContractClass.Products.PRICE_COST, pricePurchaseProduct);
						contentValues.put(ContractClass.Products.BARCODE, productBarcode);
						contentValues.put(ContractClass.Products._COUNT, count);
						contentValues.put(ContractClass.Products._ID, id);

						getContentResolver().insert(SmartShopContentProvider.PRODUCTS_CONTENT_URI, contentValues);
					}

					Toast.makeText(getApplicationContext(), "Товары успешно синхронизированы!", Toast.LENGTH_LONG)
							.show();
				}
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onCancelled() {}
	};

	private HttpsHelper.HttpsAsyncTask.HttpsAsyncTaskCallback checkCallback = new HttpsHelper.HttpsAsyncTask.HttpsAsyncTaskCallback() {
		@Override
		public void onPreExecute() {}

		@Override
		public void onPostExecute(JSONObject jsonObject) {
			try {
				if (isResponseSuccess(jsonObject.getInt(HttpsHelper.RESPONSE_CODE))) {
					DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());

					databaseHelper.dropTable(ContractClass.Сhecks.TABLE_NAME);

					JSONArray response = jsonObject.getJSONArray(HttpsHelper.RESPONSE);
					for (int i = 0; i < response.length(); ++i) {
						JSONObject checkGroup = response.getJSONObject(i);

						String time = checkGroup.getString("creation_time");

						JSONArray checks = checkGroup.getJSONArray(SmartShopUrl.Shop.Check.RESPONSE_ARRAY_NAME);

						for (int j = 0; j < checks.length(); ++j) {
							JSONObject check = checks.getJSONObject(j);

							String itemId = check.getString("item_id");
							String count = check.getString("count");
							String priceSellingProduct = check.getString("priceSellingProduct");
							String pricePurchaseProduct = check.getString("pricePurchaseProduct");

							ContentValues contentValues = new ContentValues();

							contentValues.put(ContractClass.Сhecks.ID_FROM_PRODUCTS_TABLE, itemId);
							contentValues.put(ContractClass.Сhecks._COUNT, count);
							contentValues.put(ContractClass.Сhecks.PRICE_SELLING, priceSellingProduct);
							contentValues.put(ContractClass.Сhecks.PRICE_COST, pricePurchaseProduct);
							contentValues.put(ContractClass.Сhecks.DATE_TIME, time);

							getContentResolver().insert(SmartShopContentProvider.CHECKS_CONTENT_URI, contentValues);
						}
					}

					fillChart();

					Toast.makeText(getApplicationContext(), "Чеки успешно синхронизированы!", Toast.LENGTH_LONG)
							.show();
				}
			}
			catch (JSONException e) {
				e.printStackTrace();
			}

		}

		@Override
		public void onCancelled() {}
	};
}
