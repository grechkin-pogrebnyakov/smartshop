package com.technopark.smartbiz;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class CheckActivity extends AppCompatActivity {

	private float totalPrice = 123.45f;
	public static String TOTAL_PRICE = "TotalPrice";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_check);

		Button button = (Button) findViewById(R.id.activity_check_button_submit);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent submit = new Intent(getApplicationContext(), PaymentActivity.class);
				submit.putExtra(TOTAL_PRICE, totalPrice);
				startActivity(submit);
			}
		});
	}
}
