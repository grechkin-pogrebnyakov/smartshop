package com.technopark.smartbiz;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class PurchaseActivity extends AppCompatActivity {

	private PurchaseDialogFragment purchaseDialogFragment = new PurchaseDialogFragment();
	private View.OnClickListener dialogListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			int i = purchaseDialogFragment.getProductCount();
			float f = purchaseDialogFragment.getTotalPrice();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_purchase);

		purchaseDialogFragment.setAddButtonCallback(dialogListener);

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
