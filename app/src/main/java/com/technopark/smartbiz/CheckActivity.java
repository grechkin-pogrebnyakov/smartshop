package com.technopark.smartbiz;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.technopark.smartbiz.adapters.ProductAdapter;
import com.technopark.smartbiz.database.items.Check;
import com.technopark.smartbiz.screnListView.EndlessScrollListener;

public class CheckActivity extends AppCompatActivity {

	private static int SELECT_PRODUCT = 1;

	private ListView checkList;
	private final String LOG_TAG = "CheckActivity";
	// Уникальный идентификатор загрузчика
	private ProductAdapter adapter;

	private float totalPrice = 123.45f;
	public static String TOTAL_PRICE = "TotalPrice";

	private Button checkButtonSubmit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_check);

		initializationButtons();

		checkList = (ListView) findViewById( R.id.activity_check_listview );
		adapter = new ProductAdapter( this );

		checkList.setAdapter(adapter);
		checkList.setOnScrollListener(new EndlessScrollListener() {
			@Override
			public void loadData(int offset) {

			}
		});
		Check check = new Check("hdbc", "", 34, 34, 34, 54);
		adapter.addItem(check);

	}

	private void initializationButtons () {
		checkButtonSubmit = (Button) findViewById(R.id.activity_check_button_submit);
		checkButtonSubmit.setOnClickListener(new View.OnClickListener() {
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
}
