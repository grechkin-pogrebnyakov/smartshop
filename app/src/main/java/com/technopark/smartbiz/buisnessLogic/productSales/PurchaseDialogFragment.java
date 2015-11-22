package com.technopark.smartbiz.buisnessLogic.productSales;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.technopark.smartbiz.buisnessLogic.deleteProduct.DialogFragmentCallback;
import com.technopark.smartbiz.R;
import com.technopark.smartbiz.database.items.Check;

import java.util.Locale;

/**
 * Created by titaevskiy.s on 08.11.15.
 */
public class PurchaseDialogFragment extends DialogFragment implements View.OnClickListener {

	private String productName = "";
	private float productPrice = 0.0f;
	private int productCount = 1;
	private float totalPrice = productCount * productPrice;
	private Check check;

	private View view = null;
	private TextView productNameTextView;
	private EditText countEditText;
	private TextView productPriceTextView;

	private DialogFragmentCallback addButtonCallback = null;

	private TextWatcher countTextWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			Editable editable = Editable.Factory.getInstance().newEditable(s);
			if (editable.toString().isEmpty()) {
				editable.append("0");
			}
			productCount = Integer.valueOf(editable.toString());

			updateTotalPrice();
		}
	};

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getDialog().setTitle("Количество");
		view = inflater.inflate(R.layout.dialog_purchase_add_product, null);

		productNameTextView = (TextView)
				view.findViewById(R.id.dialog_purchase_add_product_textview_product_name);

		countEditText =
				(EditText) view.findViewById(R.id.dialog_purchase_add_product_edittext_count);
		countEditText.addTextChangedListener(countTextWatcher);

		productPriceTextView =
				(TextView) view.findViewById(R.id.dialog_purchase_add_product_textview_price);

		Button addButton = (Button) view.findViewById(R.id.dialog_purchase_add_product_button_add);
		addButton.setOnClickListener(this);

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();

		productNameTextView.setText(productName);
		countEditText.setText(String.valueOf(productCount));
		productPriceTextView.setText(String.format(Locale.US, "%.2f", productPrice));

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

	public void setAddButtonCallback(DialogFragmentCallback addButtonCallback) {
		this.addButtonCallback = addButtonCallback;
	}

	public void setCheck(Check check) {
		this.check = check;
	}

	private void updateTotalPrice() {
		totalPrice = productCount * productPrice;

		TextView totalTextView = (TextView) view
				.findViewById(R.id.dialog_purchase_add_product_textview_total_price);
		totalTextView.setText(String.format(Locale.US, "%.2f", totalPrice));
	}

	public Check getCheck() {
		return check;
	}

	public float getTotalPrice() {
		return totalPrice;
	}

	public int getProductCount() {
		return productCount;
	}

	@Override
	public void onClick(View v) {
		dismiss();
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);

		addButtonCallback.callback();
	}

	public float getProductPrice() {
		return productPrice;
	}
}
