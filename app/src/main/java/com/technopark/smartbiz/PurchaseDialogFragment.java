package com.technopark.smartbiz;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Locale;

/**
 * Created by titaevskiy.s on 08.11.15.
 */
public class PurchaseDialogFragment extends DialogFragment implements View.OnClickListener, View.OnFocusChangeListener {
	private String productName = "";
	private float productPrice = 0.0f;
	private int productCount = 1;
	private float totalPrice = productCount * productPrice;

	private View view = null;
	private TextView productNameTextView;
	private EditText countEditText;
	private EditText productPriceEditText;

	private View.OnClickListener addButtonCallback = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
		}
	};

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getDialog().setTitle("Количество");
		view = inflater.inflate(R.layout.dialog_purchase_add_product, null);

		productNameTextView = (TextView) view.findViewById(R.id.dialog_purchase_add_product_textview_product_name);

		countEditText = (EditText) view.findViewById(R.id.dialog_purchase_add_product_edittext_count);
		countEditText.setOnFocusChangeListener(this);

		productPriceEditText = (EditText) view.findViewById(R.id.dialog_purchase_add_product_edittext_price);
		productPriceEditText.setOnFocusChangeListener(this);

		Button addButton = (Button) view.findViewById(R.id.dialog_purchase_add_product_button_add);
		addButton.setOnClickListener(this);

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();

		productNameTextView.setText(productName);
		countEditText.setText(String.valueOf(productCount));
		productPriceEditText.setText(String.format(Locale.US, "%.2f", productPrice));

		updateTotalPrice();
	}

	public void setProductPrice(float productPrice) {
		this.productPrice = productPrice;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public void setProductCount(int productCount) {
		this.productCount = productCount;
	}

	public void setAddButtonCallback(View.OnClickListener addButtonCallback) {
		this.addButtonCallback = addButtonCallback;
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (!hasFocus) {
			switch (v.getId()) {
				case (R.id.dialog_purchase_add_product_edittext_count):
					String productCountString = countEditText.getText().toString();
					productCount = Integer.valueOf(productCountString);

					updateTotalPrice();
					break;
				case (R.id.dialog_purchase_add_product_edittext_price):
					String productPriceString = productPriceEditText.getText().toString();
					productPrice = Float.valueOf(productPriceString);

					updateTotalPrice();
					break;
			}
		}
	}

	private void updateTotalPrice() {
		totalPrice = productCount * productPrice;

		TextView totalTextView = (TextView) view.findViewById(R.id.dialog_purchase_add_product_textview_total_price);
		totalTextView.setText(String.format(Locale.US, "%.2f", totalPrice));
	}

	public float getTotalPrice() {
		return totalPrice;
	}

	public int getProductCount() {
		return productCount;
	}

	@Override
	public void onClick(View v) {
		addButtonCallback.onClick(v);
		dismiss();
	}
}
