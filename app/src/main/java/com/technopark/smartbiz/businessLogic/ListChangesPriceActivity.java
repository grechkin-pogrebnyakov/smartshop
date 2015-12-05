package com.technopark.smartbiz.businessLogic;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;

import com.technopark.smartbiz.R;
import com.technopark.smartbiz.database.ContractClass;


public class ListChangesPriceActivity extends AppCompatActivity {

	private SimpleCursorAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_changes_price);

		final String[] from = new String[]{
				ContractClass.Employees.COLUMN_NAME_FIRST_NAME,
				ContractClass.Employees.COLUMN_NAME_LAST_NAME,
				ContractClass.Employees.COLUMN_NAME_FATHER_NAME,
				ContractClass.Employees.COLUMN_NAME_LOGIN,
		};

		final int[] to = new int[]{
				R.id.item_change_price_name_product,
				R.id.item_change_price_old_price,
				R.id.item_change_price_new_price,
		};
		adapter = new SimpleCursorAdapter(this, R.layout.employee_item_layout, null, from, to, 0);
	}
}
