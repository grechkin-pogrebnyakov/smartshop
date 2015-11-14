package com.technopark.smartbiz;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class PurchaseActivity extends AppCompatActivity {

	private String DIALOG = "purchaseDialogFragment";

	private PurchaseDialogFragment purchaseDialogFragment = new PurchaseDialogFragment();

	private View.OnClickListener dialogListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			int i = purchaseDialogFragment.getProductCount();
			float f = purchaseDialogFragment.getTotalPrice();
			Intent result = new Intent();
			// TODO Add data to result
			setResult(RESULT_OK, result);
			finish();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_purchase);

		purchaseDialogFragment.setAddButtonCallback(dialogListener);
	}

	private void showDialog() {
		purchaseDialogFragment.setProductName("qwerty");
		purchaseDialogFragment.setProductPrice(12.3f);
		purchaseDialogFragment.setProductCount(1);
		purchaseDialogFragment.show(getFragmentManager(), DIALOG);
	}
}
