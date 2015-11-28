package com.technopark.smartbiz.businessLogic.employees;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.technopark.smartbiz.R;
import com.technopark.smartbiz.database.ContractClass;

public class EmployeeListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

	public static final int LOADER_ID = 0;

	private SimpleCursorAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_employee_list);

		final String[] from = new String[]{
				ContractClass.Employees.COLUMN_NAME_FIRST_NAME,
				ContractClass.Employees.COLUMN_NAME_LAST_NAME,
				ContractClass.Employees.COLUMN_NAME_FATHER_NAME,
				ContractClass.Employees.COLUMN_NAME_LOGIN,
		};
		final int[] to = new int[]{
				R.id.employee_item_layout_first_name,
				R.id.employee_item_layout_last_name,
				R.id.employee_item_layout_father_name,
				R.id.employee_item_layout_login,
		};
		adapter = new SimpleCursorAdapter(this, R.layout.employee_item_layout, null, from, to, 0);

		ListView employeeListView = (ListView) findViewById(R.id.activity_employee_list_listview);
		employeeListView.setAdapter(adapter);
		employeeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Cursor cursor = (Cursor) adapter.getItem(position);
				final String login = cursor.getString(cursor.getColumnIndex(ContractClass.Employees.COLUMN_NAME_LOGIN));

				EmployeeInfoDialog dialog = EmployeeInfoDialog.newInstance(login);
				dialog.show(getFragmentManager(), "EmployeeInfoDialog");
			}
		});

		getLoaderManager().initLoader(LOADER_ID, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(
				this,
				ContractClass.Employees.CONTENT_URI,
				ContractClass.Employees.DEFAULT_PROJECTION,
				null,
				null,
				null
		);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}
}
