package com.technopark.smartbiz;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class PurchaseActivity extends AppCompatActivity {

	PurchaseDialogFragment purchaseDialogFragment = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_purchase);

		purchaseDialogFragment = new PurchaseDialogFragment();

		Button button = (Button) findViewById(R.id.activity_purchase_button_submit);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				purchaseDialogFragment.setProductName("qwerty");
				purchaseDialogFragment.setProductPrice(12.3f);
				purchaseDialogFragment.setProductCount(1);
				purchaseDialogFragment.show(getFragmentManager(), "purchaseDialogFragment");
			}
		});
	}


}
