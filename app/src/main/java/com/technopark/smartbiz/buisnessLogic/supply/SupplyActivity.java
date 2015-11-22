package com.technopark.smartbiz.buisnessLogic.supply;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.technopark.smartbiz.buisnessLogic.main.MainActivity;
import com.technopark.smartbiz.R;
import com.technopark.smartbiz.adapters.ProductAdapter;
import com.technopark.smartbiz.buisnessLogic.productSales.PurchaseActivity;
import com.technopark.smartbiz.database.DatabaseHelper;
import com.technopark.smartbiz.database.items.Check;
import com.technopark.smartbiz.database.items.ItemForProductAdapter;
import com.technopark.smartbiz.screnListView.EndlessScrollListener;

public class SupplyActivity extends AppCompatActivity {

	private int SELECT_PRODUCT = 1;

	private ProductAdapter adapter;
	private DatabaseHelper dbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_supply);

		dbHelper = new DatabaseHelper(this);

		adapter = new ProductAdapter(this);

		ListView checkList = (ListView) findViewById(R.id.activity_supply_listview);
		checkList.setAdapter(adapter);
		checkList.setOnScrollListener(new EndlessScrollListener() {
			@Override
			public void loadData(int offset) {
			}
		});

		Button doneSupplyButton = (Button) findViewById(R.id.activity_supply_button_done);
		doneSupplyButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				doSupply();

				Intent intent = new Intent(getApplicationContext(), MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_supply, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent addProduct = new Intent(getApplicationContext(), PurchaseActivity.class);
		addProduct.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		startActivityForResult(addProduct, SELECT_PRODUCT);
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SELECT_PRODUCT && resultCode == RESULT_OK) {
			Check check = data.getParcelableExtra(PurchaseActivity.KEY_RESPONCE_OBJECT);
			adapter.addItem(check);
			adapter.notifyDataSetChanged();
		}
	}

	private void doSupply() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		String queryString;
		String formatString = "UPDATE " + DatabaseHelper.PRODUCTS_TABLE_NAME + " " +
				"SET count = count + %d " +
				"WHERE _id = %d" +
				";";

		for (ItemForProductAdapter check : adapter.getListItems()) {
			queryString = String.format(
					formatString,
					check.getCount(),
					((Check) check).getIdFromProductsTable()
			);
			db.execSQL(queryString);

			Log.d("Supply update", queryString);
		}

		db.close();
	}
}
