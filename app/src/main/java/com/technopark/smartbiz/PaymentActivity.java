package com.technopark.smartbiz;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.technopark.smartbiz.database.SmartShopContentProvider;
import com.technopark.smartbiz.database.items.Check;
import com.technopark.smartbiz.database.items.ItemForProductAdapter;

import java.util.ArrayList;
import java.util.Locale;

public class PaymentActivity extends AppCompatActivity implements TextWatcher {

	private float totalPrice = 0.0f;
	private float oddMoney = 0.0f;
	private float payment = 0.0f;

	private EditText paymentEditText;
	private TextView oddMoneyTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_payment);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			totalPrice = extras.getInt(CheckActivity.TOTAL_PRICE);
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
					Intent submit = new Intent(getApplicationContext(), MainActivity.class);
					submit.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					addRecord(getIntent().getParcelableArrayListExtra(CheckActivity.CHECK_LIST_NAME));
					startActivity(submit);
				}
			}
		});
	}

	private void updateOddMoneyEditText() {
		oddMoney = payment - totalPrice;
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

		this.payment = Float.valueOf(paymentString.toString());
		updateOddMoneyEditText();
	}

	private Uri addRecord(ArrayList<Parcelable> checkList) {
		// Defines a new Uri object that receives the result of the insertion
		Uri mNewUri = null;
		Check check;
		// Defines an object to contain the new values to insert
		ContentValues mNewValues = new ContentValues();

        /*
        * Sets the values of each column and inserts the word. The arguments to the "put"
        * method are "column name" and "value"
        */

		for (Parcelable temp : checkList) {
			check = (Check) temp;
			mNewValues.put("id_from_products_table", check.getIdFromProductsTable());
			mNewValues.put("photo_path", check.getPhotoPath());
			mNewValues.put("name", check.getProductName());
			mNewValues.put("price_selling_product", check.getPriceSellingProduct());
			mNewValues.put("price_cost_product", check.getPricePurchaseProduct());
			mNewValues.put("count", check.getCount());

			mNewUri = getContentResolver().insert(
					SmartShopContentProvider.PRODUCTS_CONTENT_URI,   // the user dictionary content URI
					mNewValues                          // the values to insert
			);
		}
		return mNewUri;
	}
}
