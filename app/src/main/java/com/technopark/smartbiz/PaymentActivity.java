package com.technopark.smartbiz;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Locale;

public class PaymentActivity extends AppCompatActivity implements View.OnFocusChangeListener {

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
			totalPrice = extras.getFloat(CheckActivity.TOTAL_PRICE);
		}

		TextView totalTextView = (TextView) findViewById(R.id.activity_payment_textview_total);
		totalTextView.setText(String.format(Locale.US, "%.2f", totalPrice));

		oddMoneyTextView = (TextView) findViewById(R.id.activity_payment_textview_odd_money);
		updateOddMoneyEditText();

		paymentEditText = (EditText) findViewById(R.id.activity_payment_edittext_payment);
		paymentEditText.setOnFocusChangeListener(this);

		Button doneButton = (Button) findViewById(R.id.activity_payment_button_pay);
		doneButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent submit = new Intent(getApplicationContext(), MainActivity.class);
				submit.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(submit);
			}
		});
	}

	private void updateOddMoneyEditText() {
		oddMoney = payment - totalPrice;
		oddMoneyTextView.setText(String.format(Locale.US, "%.2f", oddMoney));
	}

	// TODO Change to key press event
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (!hasFocus) {
			payment = Float.valueOf(paymentEditText.getText().toString());
			updateOddMoneyEditText();
		}
	}
}
