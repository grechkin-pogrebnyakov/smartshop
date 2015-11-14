package com.technopark.smartbiz;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_check, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent addProduct = new Intent(getApplicationContext(), PurchaseActivity.class);
		addProduct.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		startActivityForResult(addProduct, 1);
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Implement activity result
		super.onActivityResult(requestCode, resultCode, data);
	}
}
