package com.technopark.smartbiz.businessLogic.supply;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.technopark.smartbiz.ActivityWithNavigationDrawer;
import com.technopark.smartbiz.HomeProxyActivity;
import com.technopark.smartbiz.R;
import com.technopark.smartbiz.Utils;
import com.technopark.smartbiz.adapters.ProductAdapter;
import com.technopark.smartbiz.api.HttpsHelper;
import com.technopark.smartbiz.api.SmartShopUrl;
import com.technopark.smartbiz.businessLogic.productSales.DialogFragmentCallback;
import com.technopark.smartbiz.businessLogic.productSales.PurchaseActivity;
import com.technopark.smartbiz.businessLogic.productSales.PurchaseDialogFragment;
import com.technopark.smartbiz.businessLogic.showProducts.EndlessScrollListener;
import com.technopark.smartbiz.database.ContractClass;
import com.technopark.smartbiz.database.DatabaseHelper;
import com.technopark.smartbiz.database.SmartShopContentProvider;
import com.technopark.smartbiz.database.items.Check;
import com.technopark.smartbiz.database.items.ItemForProductAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SupplyActivity extends ActivityWithNavigationDrawer implements HttpsHelper.HttpsAsyncTask.HttpsAsyncTaskCallback {

	private int SELECT_PRODUCT = 1;
	private String DIALOG = "purchaseDialogFragment";

	private ProductAdapter adapter;
	private DatabaseHelper dbHelper;

	private View progressView;
	private View supplyActionView;

	private PurchaseDialogFragment purchaseDialogFragment = new PurchaseDialogFragment();

	private DialogFragmentCallback dialogListener = new DialogFragmentCallback() {
		@Override
		public void callback() {
			Check check = purchaseDialogFragment.getCheck();
			check.setCount(purchaseDialogFragment.getProductCount());
			check.setPriceSellingProduct(purchaseDialogFragment.getProductPrice());
			adapter.addItem(check);
			adapter.notifyDataSetChanged();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_supply);

		progressView = findViewById(R.id.activity_supply_progress_view);
		supplyActionView = findViewById(R.id.activity_supply_view_supply_action);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		setDrawerToolbar(toolbar);

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
			}
		});
		purchaseDialogFragment.setAddButtonCallback(dialogListener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_supply, menu);
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
		//		Intent addProduct = new Intent(getApplicationContext(), PurchaseActivity.class);
		//		addProduct.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		//		startActivityForResult(addProduct, SELECT_PRODUCT);
		//		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SELECT_PRODUCT && resultCode == RESULT_OK) {
			Check check = data.getParcelableExtra(PurchaseActivity.KEY_RESPONCE_OBJECT);
			adapter.addItem(check);
			adapter.notifyDataSetChanged();
		}

		IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
		if (result != null) {
			String contents = result.getContents();
			if (contents != null) {
				String barcode = result.getContents();
				findProductByBarcode(barcode);
			}
			else {
				Toast.makeText(getApplicationContext(), "Не отсканировано !",
						Toast.LENGTH_LONG).show();
			}
		}
	}

	private void doSupply() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		String queryString;
		String formatString = "UPDATE " + ContractClass.Products.TABLE_NAME + " " +
				"SET _count = _count + %d " +
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

		sendToServer();
	}

	private void sendToServer() {
		JSONObject requestJsonObject = new JSONObject();
		JSONObject tempCheck = new JSONObject();

		JSONArray tempArray = new JSONArray();
		try {
			for (ItemForProductAdapter check : adapter.getListItems()) {

				tempCheck.put("price_id", ((Check) check).getPriceId());
				tempCheck.put("count", check.getCount());

				tempArray.put(tempCheck);
			}

			requestJsonObject.put(SmartShopUrl.Shop.Check.REQUEST_ARRAY_NAME, tempArray);
			requestJsonObject.put("type", SmartShopUrl.Shop.Check.TYPE_SUPPLY);
		}
		catch (JSONException e) {
			e.printStackTrace();
		}

		new HttpsHelper.HttpsAsyncTask(SmartShopUrl.Shop.Check.URL_CHECK_ADD, requestJsonObject, this, this)
				.execute(HttpsHelper.Method.POST);
	}

	private void scanBarcode() {
		IntentIntegrator integrator = new IntentIntegrator(SupplyActivity.this);
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

	@Override
	public void onPreExecute() {
		Utils.showProgress(true, supplyActionView, progressView, SupplyActivity.this);
	}

	@Override
	public void onPostExecute(JSONObject jsonObject) {
		Intent intent = new Intent(getApplicationContext(), HomeProxyActivity.class);
		startActivity(intent);
		Utils.showProgress(false, supplyActionView, progressView, SupplyActivity.this);
	}

	@Override
	public void onCancelled() {
		Utils.showProgress(false, supplyActionView, progressView, SupplyActivity.this);
	}
}
