package com.technopark.smartbiz.businessLogic.productSales;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.technopark.smartbiz.MainActivity;
import com.technopark.smartbiz.R;
import com.technopark.smartbiz.api.HttpsHelper;
import com.technopark.smartbiz.api.SmartShopUrl;
import com.technopark.smartbiz.database.ContractClass;
import com.technopark.smartbiz.database.DatabaseHelper;
import com.technopark.smartbiz.database.SmartShopContentProvider;
import com.technopark.smartbiz.database.items.Check;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class PaymentActivity extends AppCompatActivity implements TextWatcher, HttpsHelper.HttpsAsyncTask.HttpsAsyncTaskCallback {

	private double totalPrice = 0.0f;
	private double oddMoney = 0.0f;
	private double payment = 0.0f;

	private EditText paymentEditText;
	private TextView oddMoneyTextView;
	private DatabaseHelper dbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_payment);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			totalPrice = extras.getDouble(CheckActivity.TOTAL_PRICE);
		}

		TextView totalTextView = (TextView) findViewById(R.id.activity_payment_textview_total);
		totalTextView.setText(String.format(Locale.US, "%.2f", totalPrice));

		oddMoneyTextView = (TextView) findViewById(R.id.activity_payment_textview_odd_money);
		updateOddMoneyEditText();

		paymentEditText = (EditText) findViewById(R.id.activity_payment_edittext_payment);
		paymentEditText.addTextChangedListener(this);

		Button doneButton = (Button) findViewById(R.id.activity_payment_button_pay);
		doneButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (oddMoney >= 0) {
					ArrayList<Check> checkArrayList =
							getIntent().getParcelableArrayListExtra(CheckActivity.CHECK_LIST_NAME);
					addRecord(checkArrayList);
					updateProductsDatabase(checkArrayList);

					sendToServer(checkArrayList);
				}
			}
		});

		dbHelper = new DatabaseHelper(this);
	}

	private void sendToServer(ArrayList<Check> checkArrayList) {
		JSONObject requestJsonObject = new JSONObject();
		JSONObject tempCheck = new JSONObject();

		JSONArray tempArray = new JSONArray();
		try {
			for (Check check : checkArrayList) {

				tempCheck.put("item_id", check.getIdFromProductsTable());
				tempCheck.put("count", check.getCount());

				tempArray.put(tempCheck);
			}

			requestJsonObject.put(SmartShopUrl.Shop.Check.REQUEST_ARRAY_NAME, tempArray);
			requestJsonObject.put("type", SmartShopUrl.Shop.Check.TYPE_SELL);
		}
		catch (JSONException e) {
			e.printStackTrace();
		}

		new HttpsHelper.HttpsAsyncTask(SmartShopUrl.Shop.Check.URL_CHECK_ADD, requestJsonObject, this, this)
				.execute(HttpsHelper.Method.POST);
	}

	private void updateProductsDatabase(ArrayList<Check> checkArrayList) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		String queryString;
		String formatString = "UPDATE " + ContractClass.Products.TABLE_NAME + " " +
				"SET _count = _count - %d " +
				"WHERE _id = %d" +
				";";

		for (Check check : checkArrayList) {
			queryString = String.format(
					formatString,
					check.getCount(),
					check.getIdFromProductsTable()
			);
			db.execSQL(queryString);

			Log.d("Payment update", queryString);
		}

		db.close();
	}

	private void updateOddMoneyEditText() {
		oddMoney = payment - totalPrice;
		if (Math.abs(oddMoney) < 0.001) {
			oddMoney = 0;
		}
		oddMoneyTextView.setText(String.format(Locale.US, "%.2f", oddMoney));
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	@Override
	public void afterTextChanged(Editable s) {
		Editable paymentString = Editable.Factory.getInstance().newEditable(s);
		if (paymentString.toString().isEmpty()) {
			paymentString.append("0");
		}

		this.payment = Double.valueOf(paymentString.toString());
		updateOddMoneyEditText();
	}

	private Uri addRecord(ArrayList<Check> checkList) {
		// Defines a new Uri object that receives the result of the insertion
		Uri mNewUri = null;
		Check check;
		// Defines an object to contain the new values to insert
		ContentValues mNewValues = new ContentValues();

        /*
        * Sets the values of each column and inserts the word. The arguments to the "put"
        * method are "column name" and "value"
        */

		for (Check temp : checkList) {
			check = temp;
			mNewValues.put(ContractClass.Сhecks.ID_FROM_PRODUCTS_TABLE, check.getIdFromProductsTable());
			mNewValues.put(ContractClass.Сhecks.PHOTO_PATH, check.getPhotoPath());
			mNewValues.put(ContractClass.Сhecks.NAME, check.getProductName());
			mNewValues.put(ContractClass.Сhecks.PRICE_SELLING, check.getPriceSellingProduct());
			mNewValues.put(ContractClass.Сhecks.PRICE_COST, check.getPricePurchaseProduct());
			mNewValues.put(ContractClass.Сhecks._COUNT, check.getCount());

			mNewUri = getContentResolver().insert(
					SmartShopContentProvider.CHECKS_CONTENT_URI,   // the user dictionary content URI
					mNewValues                          // the values to insert
			);
		}
		getContentResolver().notifyChange(SmartShopContentProvider.CHECKS_CONTENT_URI, null);
		return mNewUri;
	}

	@Override
	public void onPreExecute() {}

	@Override
	public void onPostExecute(JSONObject jsonObject) {
		Intent submit = new Intent(getApplicationContext(), MainActivity.class);
		submit.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(submit);
	}

	@Override
	public void onCancelled() {

	}
}
