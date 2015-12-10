package com.technopark.smartbiz.businessLogic.productSales;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.technopark.smartbiz.ActivityWithNavigationDrawer;
import com.technopark.smartbiz.R;
import com.technopark.smartbiz.adapters.ProductAdapter;
import com.technopark.smartbiz.businessLogic.showProducts.EndlessScrollListener;
import com.technopark.smartbiz.database.ContractClass;
import com.technopark.smartbiz.database.SmartShopContentProvider;
import com.technopark.smartbiz.database.items.Check;
import com.technopark.smartbiz.database.items.ItemForProductAdapter;

import java.util.ArrayList;
import java.util.Locale;


public class CheckActivity extends ActivityWithNavigationDrawer {

	private static int SELECT_PRODUCT = 1;
	public static String KEY_RESPONCE_OBJECT = "check";
	private String DIALOG = "purchaseDialogFragment";

	private ListView checkList;
	private final String LOG_TAG = "CheckActivity";
	// Уникальный идентификатор загрузчика
	private ProductAdapter adapter;

	public static String TOTAL_PRICE = "TotalPrice";
	public static String CHECK_LIST_NAME = "CheckListName";

	private String barcode;
	private Button checkButtonSubmit;

	private PurchaseDialogFragment purchaseDialogFragment = new PurchaseDialogFragment();

	private DialogFragmentCallback dialogListener = new DialogFragmentCallback() {
		@Override
		public void callback() {
			Check check = purchaseDialogFragment.getCheck();
			check.setCount(purchaseDialogFragment.getProductCount());
			check.setPriceSellingProduct(purchaseDialogFragment.getProductPrice());
			adapter.addItem(check);
			adapter.notifyDataSetChanged();
			setTotalPrice(calculateSumCheck());
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_check);

		initializationButtons();

		checkList = (ListView) findViewById(R.id.activity_check_listview);
		adapter = new ProductAdapter(this);

		checkList.setAdapter(adapter);
		checkList.setOnScrollListener(new EndlessScrollListener() {
			@Override
			public void loadData(int offset) {

			}
		});
		purchaseDialogFragment.setAddButtonCallback(dialogListener);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		setDrawerToolbar(toolbar);
	}

	private void initializationButtons() {
		checkButtonSubmit = (Button) findViewById(R.id.activity_check_button_submit);
		checkButtonSubmit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent submit = new Intent(getApplicationContext(), PaymentActivity.class);
				submit.putExtra(TOTAL_PRICE, calculateSumCheck());
				submit.putParcelableArrayListExtra(CHECK_LIST_NAME, getCheckList());
				startActivity(submit);
			}
		});
	}

	private ArrayList<? extends Parcelable> getCheckList() {
		ArrayList<Check> checkList = new ArrayList<>();
		for (ItemForProductAdapter temp : adapter.getListItems()) {
			checkList.add((Check) temp);
		}
		return checkList;
	}

	private double calculateSumCheck() {
		double sum = 0;
		for (ItemForProductAdapter temp : adapter.getListItems()) {
			sum += temp.getPriceSellingProduct() * temp.getCount();
		}
		return sum;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_check, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.add_product:
				Intent addProduct = new Intent(getApplicationContext(), PurchaseActivity.class);
				addProduct.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				startActivityForResult(addProduct, SELECT_PRODUCT);
				return false;
			case R.id.scan_product:
				scanBarcode();
				return false;
			default:
				return false;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SELECT_PRODUCT && resultCode == RESULT_OK) {
			Check check = data.getParcelableExtra(PurchaseActivity.KEY_RESPONCE_OBJECT);
			adapter.addItem(check);
			adapter.notifyDataSetChanged();
			setTotalPrice(calculateSumCheck());
		}
		IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
		if (result != null) {
			String contents = result.getContents();
			if (contents != null) {
				barcode = result.getContents();
				findProductByBarcode(barcode);
			}
			else {
				Toast.makeText(getApplicationContext(), "Не отсканировано !",
						Toast.LENGTH_LONG).show();
			}
		}
	}

	private void setTotalPrice(double totalPrice) {
		TextView totalTextView = (TextView) findViewById(R.id.activity_check_total);
		totalTextView.setText(String.format(Locale.US, "%.2f", totalPrice));
	}

	private void scanBarcode() {
		IntentIntegrator integrator = new IntentIntegrator(CheckActivity.this);
		integrator.initiateScan(IntentIntegrator.ALL_CODE_TYPES);
	}

	private void findProductByBarcode(String barcode) {
		Cursor cursor = getContentResolver().query(SmartShopContentProvider.PRODUCTS_CONTENT_URI, new String[]{},
				"barcode = ?", new String[]{barcode}, "");
		if (cursor != null && cursor.moveToNext()) {
			String nameProduct = cursor.getString(cursor.getColumnIndex(ContractClass.Products.NAME));
			String photoPath = cursor.getString(cursor.getColumnIndex(ContractClass.Products.PHOTO_PATH));
			double priceSellingProduct = cursor.getDouble(cursor
					.getColumnIndex(ContractClass.Products.PRICE_SELLING));
			double pricePurchaseProduct = cursor.getDouble(cursor
					.getColumnIndex(ContractClass.Products.PRICE_COST));
			int countProduct = cursor.getInt(cursor.getColumnIndex(ContractClass.Products._COUNT));
			long id = cursor.getLong(cursor.getColumnIndex(ContractClass.Products._ID));
			long priceId = cursor.getLong(cursor.getColumnIndex(ContractClass.Products.PRICE_ID));
			Check check = new Check(nameProduct, photoPath, priceSellingProduct, pricePurchaseProduct, id, priceId, countProduct);
			showDialog(check);
		}
		else {
			Toast.makeText(getApplicationContext(), "Продукт не найден !",
					Toast.LENGTH_LONG).show();
		}
	}

	private void showDialog(Check check) {
		purchaseDialogFragment.setCheck(check);
		purchaseDialogFragment.setProductName(check.getProductName());
		purchaseDialogFragment.setProductPrice(check.getPriceSellingProduct());
		purchaseDialogFragment.setProductCount(1);
		purchaseDialogFragment.show(getFragmentManager(), DIALOG);
	}
}
