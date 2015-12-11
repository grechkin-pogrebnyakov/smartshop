package com.technopark.smartbiz;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.mikepenz.materialdrawer.Drawer;
import com.technopark.smartbiz.api.HttpsHelper;
import com.technopark.smartbiz.api.SmartShopUrl;
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


public class MainActivity extends ActivityWithNavigationDrawer implements InteractionWithUI {

	public static final String APP_PREFERENCES = "mysettings";

	private SharedPreferences sharedPreferences;
	private CheckContentObserver checkContentObserver = new CheckContentObserver();
	private DatabaseHelper dbHelper;

	private AccessControl accessControl;
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
		setDrawerToolbar(toolbar);

		accessControl = new AccessControl(getApplicationContext(), this, UserIdentificationContract.REQUEST_CODE_ACCESS_LOGIN);
		accessControl.displayActivityOfAccessRights();

		// Start IntentService to register this application with GCM.
		Intent intent = new Intent(this, RegistrationIntentService.class);
		startService(intent);
	}

	@Override
	public void netActionResponse(int requestActionCode, JSONObject jsonResponse) {
		switch (requestActionCode) {
			case UserIdentificationContract.REQUEST_CODE_LOG_OUT_ACTION:
				logOutResultAction(jsonResponse);
				break;
		}
	}

	private void logOutResultAction(JSONObject resultActionCode) {
		try {
			if (resultActionCode.has(UserIdentificationContract.LOG_OUT_RESPONSE_STATUS_KEY)) {
				switch (resultActionCode.getInt(UserIdentificationContract.LOG_OUT_RESPONSE_STATUS_KEY)) {
					case UserIdentificationContract.LOGOUT_STATUS_SUCCESS:
						sharedPreferences.edit().remove(UserIdentificationContract.STATUS_AUTHORIZATION_KEY).apply();
						sharedPreferences.edit().remove(UserIdentificationContract.TOKEN_AUTHORIZATION).apply();
						startActivity(new Intent(getApplicationContext(), LoginActivity.class));
						break;
					case UserIdentificationContract.LOGOUT_STATUS_FAIL:
						sharedPreferences.edit().remove(UserIdentificationContract.STATUS_AUTHORIZATION_KEY).apply();
						sharedPreferences.edit().remove(UserIdentificationContract.TOKEN_AUTHORIZATION).apply();
						startActivity(new Intent(getApplicationContext(), LoginActivity.class));
						break;
				}
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
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
				.query(ContractClass.Сhecks.TABLE_NAME, columns, null, null, "date", null, null);

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
	public void callbackAccessControl(int requestActionCode, String accessRightIdentificator) {
		switch (requestActionCode) {
			case UserIdentificationContract.REQUEST_CODE_ACCESS_LOGIN:
				initializationActivityByStatus(accessRightIdentificator);
				break;
		}
	}

	@Override
	public void showToast(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	}

	private void initializationActivityByStatus(String accessRightIdentificator) {
		initializationActivityElementsForOwner();
		switch (accessRightIdentificator) {
			case UserIdentificationContract.SUCCESS_AUTHORIZATION_EMPLOYEE:
				retainElementsForEmployee();
				break;
		}
	}

	private void initializationActivityElementsForOwner() {
		// общие элементы

		//элементы для владельца
		mainChart = (LineChart) findViewById(R.id.content_main_chart);
		setupChart();
		fillChart();
	}

	private void retainElementsForEmployee() {
		mainChart.setVisibility(View.GONE);
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
		public void onPreExecute() {
		}

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
						String productBarcode = product.getString("productBarcode");
						String count = product.getString("count");
						String id = product.getString("id");
						String priceId = product.getString("price_id");
						String imageUrl = product.getString("image_url");

						JSONObject price = product.getJSONObject("price");
						String priceSellingProduct = price.getString("priceSellingProduct");
						String pricePurchaseProduct = price.getString("pricePurchaseProduct");

						ContentValues contentValues = new ContentValues();

						contentValues.put(ContractClass.Products.NAME, productName);
						contentValues.put(ContractClass.Products.DESCRIPTION, descriptionProduct);
						contentValues.put(ContractClass.Products.PRICE_SELLING, priceSellingProduct);
						contentValues.put(ContractClass.Products.PRICE_COST, pricePurchaseProduct);
						contentValues.put(ContractClass.Products.BARCODE, productBarcode);
						contentValues.put(ContractClass.Products._COUNT, count);
						contentValues.put(ContractClass.Products._ID, id);
						contentValues.put(ContractClass.Products.PRICE_ID, priceId);
						contentValues.put(ContractClass.Products.PHOTO_PATH, imageUrl);

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
		public void onCancelled() {
		}
	};

	private HttpsHelper.HttpsAsyncTask.HttpsAsyncTaskCallback checkCallback = new HttpsHelper.HttpsAsyncTask.HttpsAsyncTaskCallback() {
		@Override
		public void onPreExecute() {
		}

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

							JSONObject price = check.getJSONObject("price");
							String priceSellingProduct = price.getString("priceSellingProduct");
							String pricePurchaseProduct = price.getString("pricePurchaseProduct");

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
		public void onCancelled() {
		}
	};
}
