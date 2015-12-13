package com.technopark.smartbiz;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.technopark.smartbiz.api.HttpsHelper;
import com.technopark.smartbiz.api.SmartShopUrl;
import com.technopark.smartbiz.businessLogic.userIdentification.AccessControl;
import com.technopark.smartbiz.businessLogic.userIdentification.InteractionWithUI;
import com.technopark.smartbiz.businessLogic.userIdentification.UserIdentificationContract;
import com.technopark.smartbiz.businessLogic.userIdentification.activities.LoginActivity;
import com.technopark.smartbiz.database.ContractClass;
import com.technopark.smartbiz.database.DatabaseHelper;
import com.technopark.smartbiz.database.SmartShopContentProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import static com.technopark.smartbiz.Utils.isResponseSuccess;


public class MainActivity extends ActivityWithNavigationDrawer implements InteractionWithUI {

	public static final String APP_PREFERENCES = "mysettings";

	private SharedPreferences sharedPreferences;
	private CheckContentObserver checkContentObserver = new CheckContentObserver();
	private DatabaseHelper dbHelper;

	private AccessControl accessControl;
	private LineChart mainChart;

	TextView circulationTextView, revenueTextView;
	EditText dateStartStatisticsCalculateEditText, dateEndStatisticsCalculateEditText,
			currentDataEditText;
	private Calendar dateAndTime = Calendar.getInstance();
	private Calendar dateStart, dateEnd, currentDateLink;

	private Menu menu;

	private ImageView refreshImageView;
	private Animation rotateAnimation;
	private boolean isInRefresh = false;

	private static boolean isInSession = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		dbHelper = new DatabaseHelper(this);

		sharedPreferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
		circulationTextView = (TextView) findViewById(R.id.activity_main_circulatioin_textView);
		revenueTextView = (TextView) findViewById(R.id.activity_main_revenue_textView);

		dateStartStatisticsCalculateEditText = (EditText) findViewById(R.id.activity_main_date_begin_calculation_editText);
		dateEndStatisticsCalculateEditText = (EditText) findViewById(R.id.activity_main_date_end_calculation_editText);

		dateStartStatisticsCalculateEditText.setInputType(InputType.TYPE_NULL);
		dateEndStatisticsCalculateEditText.setInputType(InputType.TYPE_NULL);

		currentDataEditText = dateEndStatisticsCalculateEditText;
		setInitialDateTime();
		dateEnd = Calendar.getInstance();
		dateEnd.setTime(dateAndTime.getTime());
		dateAndTime.set(Calendar.DAY_OF_MONTH, dateAndTime.get(Calendar.DAY_OF_MONTH) - 7);
		currentDataEditText = dateStartStatisticsCalculateEditText;
		setInitialDateTime();
		dateStart = Calendar.getInstance();
		dateStart.setTime(dateAndTime.getTime());

		calculateStatistics();

		dateStartStatisticsCalculateEditText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				currentDateLink = dateStart;
				setDate(dateStartStatisticsCalculateEditText);
				//				dateStart.setTime(dateAndTime.getTime());
			}
		});

		dateEndStatisticsCalculateEditText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setDate(dateEndStatisticsCalculateEditText);
				currentDateLink = dateEnd;
				//				dateEnd.setTime(dateAndTime.getTime());
			}
		});

		getContentResolver()
				.registerContentObserver(SmartShopContentProvider.CHECKS_CONTENT_URI, false, checkContentObserver);
		dbHelper = new DatabaseHelper(this);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		setDrawerToolbar(toolbar);

		accessControl = new AccessControl(getApplicationContext(), this, UserIdentificationContract.REQUEST_CODE_ACCESS_LOGIN);
		accessControl.displayActivityOfAccessRights();
	}

	private void calculateStatistics() {
		circulationTextView.setText(String.format(Locale.US, "%.2f", getCirculateBetween()) + " руб.");
		revenueTextView.setText(String.format(Locale.US, "%.2f", getRevenueBetween()) + " руб.");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);

		this.menu = menu;

		if (!isInSession) {
			isInSession = true;
			syncData();
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean out;

		switch (item.getItemId()) {
			case R.id.refresh:
				startRefreshAnimation();
				syncData();
				out = true;
				break;
			default:
				out = super.onOptionsItemSelected(item);
				break;
		}

		return out;
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

	private void startRefreshAnimation() {
		if (!isInRefresh) {
			isInRefresh = true;

			if (refreshImageView == null) {
				LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
				refreshImageView = (ImageView) inflater.inflate(R.layout.iv_refresh, null);
			}

			if (rotateAnimation == null) {
				rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh);
				rotateAnimation.setRepeatCount(Animation.INFINITE);
			}

			refreshImageView.startAnimation(rotateAnimation);

			MenuItem refreshMenuItem = menu.findItem(R.id.refresh);
			refreshMenuItem.setActionView(refreshImageView);
		}
	}

	private void stopRefreshAnimation() {
		if (isInRefresh) {
			isInRefresh = false;

			MenuItem refreshMenuItem = menu.findItem(R.id.refresh);

			if (refreshImageView != null) {
				refreshImageView.clearAnimation();
			}

			refreshMenuItem.setActionView(null);
		}
	}

	private void syncData() {
		new HttpsHelper.HttpsAsyncTask(
				SmartShopUrl.Shop.Item.URL_ITEM_LIST,
				null,
				productCallback,
				this
		).execute(HttpsHelper.Method.GET);

		// TODO Calculate time
		new HttpsHelper.HttpsAsyncTask(
				SmartShopUrl.Shop.Check.URL_CHECK_LIST + "?time1=12099242&time2=12209039393&type=0",
				null,
				checkCallback,
				this
		).execute(HttpsHelper.Method.GET);

		syncEmployees();
	}

	private void syncEmployees() {
		new HttpsHelper.HttpsAsyncTask(
				SmartShopUrl.Employee.URL_EMPLOYEE_LIST,
				null,
				employeesCallback,
				this
		).execute(HttpsHelper.Method.GET);
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

			stopRefreshAnimation();
		}

		@Override
		public void onCancelled() {
			stopRefreshAnimation();
		}
	};

	private HttpsHelper.HttpsAsyncTask.HttpsAsyncTaskCallback checkCallback = new HttpsHelper.HttpsAsyncTask.HttpsAsyncTaskCallback() {
		@Override
		public void onPreExecute() {
			startRefreshAnimation();
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

			stopRefreshAnimation();
		}

		@Override
		public void onCancelled() {
			stopRefreshAnimation();
		}
	};

	private double getCirculateBetween() {
		double circulation = 0;
		dateStart.set(Calendar.HOUR_OF_DAY, 0);
		dateStart.set(Calendar.MINUTE, 0);
		dateStart.set(Calendar.SECOND, 0);
		dateEnd.set(Calendar.HOUR_OF_DAY, 23);
		dateEnd.set(Calendar.MINUTE, 59);
		dateEnd.set(Calendar.SECOND, 59);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		String dateStartString = df.format(dateStart.getTime());
		String dateEndString = df.format(dateEnd.getTime());

		String[] columns = new String[]{
				"sum(price_selling_product * _count) AS circulate",
		};

		String selection = "date_time >= '" +
				dateStartString + "' AND " + "date_time <= '" + dateEndString + "'";

		Cursor cursor = db
				.query(ContractClass.Сhecks.TABLE_NAME, columns, selection, null, null, null, null);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				int setSize = cursor.getCount();
				for (int i = 0; i < setSize; ++i) {
					circulation = cursor.getDouble(cursor.getColumnIndex("circulate"));
				}
			}
		}
		return circulation;
	}

	private double getRevenueBetween() {
		double revenue = 0;
		dateStart.set(Calendar.HOUR_OF_DAY, 0);
		dateStart.set(Calendar.MINUTE, 0);
		dateStart.set(Calendar.SECOND, 0);
		dateEnd.set(Calendar.HOUR_OF_DAY, 23);
		dateEnd.set(Calendar.MINUTE, 59);
		dateEnd.set(Calendar.SECOND, 59);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		String dateStartString = df.format(dateStart.getTime());
		String dateEndString = df.format(dateEnd.getTime());

		String[] columns = new String[]{
				"sum(price_selling_product * _count) - sum(price_cost * _count) AS revenue",
		};

		String selection = "date_time >= '" +
				dateStartString + "' AND " + "date_time <= '" + dateEndString + "'";

		Cursor cursor = db
				.query(ContractClass.Сhecks.TABLE_NAME, columns, selection, null, null, null, null);

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				int setSize = cursor.getCount();
				for (int i = 0; i < setSize; ++i) {
					revenue = cursor.getDouble(cursor.getColumnIndex("revenue"));
				}
			}
		}

		return revenue;
	}

	// отображаем диалоговое окно для выбора даты
	public void setDate(EditText editText) {
		currentDataEditText = editText;
		new DatePickerDialog(MainActivity.this, d,
				dateAndTime.get(Calendar.YEAR),
				dateAndTime.get(Calendar.MONTH),
				dateAndTime.get(Calendar.DAY_OF_MONTH))
				.show();
	}

	// установка начальных даты и времени
	private void setInitialDateTime() {
		currentDataEditText.setText(DateUtils.formatDateTime(this,
				dateAndTime.getTimeInMillis(),
				DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));
	}

	// установка обработчика выбора даты
	DatePickerDialog.OnDateSetListener d = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			dateAndTime.set(Calendar.YEAR, year);
			dateAndTime.set(Calendar.MONTH, monthOfYear);
			dateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			if (dateAndTime.after(Calendar.getInstance())) {
				showToast("Дата не может указывать на будущее !");
				dateAndTime = Calendar.getInstance();
			}
			currentDateLink.setTime(dateAndTime.getTime());
			setInitialDateTime();
			calculateStatistics();
		}
	};

	private HttpsHelper.HttpsAsyncTask.HttpsAsyncTaskCallback employeesCallback = new HttpsHelper.HttpsAsyncTask.HttpsAsyncTaskCallback() {
		@Override
		public void onPreExecute() {
			startRefreshAnimation();
		}

		@Override
		public void onPostExecute(JSONObject jsonObject) {
			try {
				if (isResponseSuccess(jsonObject.getInt(HttpsHelper.RESPONSE_CODE))) {
					DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());

					databaseHelper.dropTable(ContractClass.Employees.TABLE_NAME);

					JSONArray response = jsonObject.getJSONArray(SmartShopUrl.Employee.RESPONSE_ARRAY_NAME);
					for (int i = 0; i < response.length(); ++i) {
						JSONObject employee = response.getJSONObject(i);

						String firstName = employee.getString("first_name");
						String lastName = employee.getString("last_name");
						String fatherName = employee.getString("father_name");
						String login = employee.getString("login");

						ContentValues contentValues = new ContentValues();

						contentValues.put(ContractClass.Employees.COLUMN_NAME_FIRST_NAME, firstName);
						contentValues.put(ContractClass.Employees.COLUMN_NAME_LAST_NAME, lastName);
						contentValues.put(ContractClass.Employees.COLUMN_NAME_FATHER_NAME, fatherName);
						contentValues.put(ContractClass.Employees.COLUMN_NAME_LOGIN, login);

						getContentResolver().insert(ContractClass.Employees.CONTENT_URI, contentValues);
					}
				}
			}

			catch (
					JSONException e
					)

			{
				e.printStackTrace();
			}

			stopRefreshAnimation();
		}

		@Override
		public void onCancelled() {
			stopRefreshAnimation();
		}
	};

}
