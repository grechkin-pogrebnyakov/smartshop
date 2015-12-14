package com.technopark.smartbiz.businessLogic.productSales;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.technopark.smartbiz.ActivityWithNavigationDrawer;
import com.technopark.smartbiz.R;
import com.technopark.smartbiz.adapters.ProductAdapter;
import com.technopark.smartbiz.api.HttpsHelper;
import com.technopark.smartbiz.api.SmartShopUrl;
import com.technopark.smartbiz.businessLogic.deleteProduct.DeleteAnimationListenter;
import com.technopark.smartbiz.businessLogic.deleteProduct.DeleteProductFromListDialogFragment;
import com.technopark.smartbiz.businessLogic.deleteProduct.SwipeDetector;
import com.technopark.smartbiz.businessLogic.showProducts.EndlessScrollListener;
import com.technopark.smartbiz.businessLogic.userIdentification.activities.LoginActivity;
import com.technopark.smartbiz.database.ContractClass;
import com.technopark.smartbiz.database.DatabaseHelper;
import com.technopark.smartbiz.database.SmartShopContentProvider;
import com.technopark.smartbiz.database.items.Check;
import com.technopark.smartbiz.database.items.ItemForProductAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import static com.technopark.smartbiz.Utils.isResponseSuccess;


public class CheckActivity extends ActivityWithNavigationDrawer implements
		DeleteProductFromListDialogFragment.NoticeDialogListener {

	private static int SELECT_PRODUCT = 1;
	public static String KEY_RESPONCE_OBJECT = "check";
	private String DIALOG = "purchaseDialogFragment";

	private ListView checkList;
	private final String LOG_TAG = "CheckActivity";
	// Уникальный идентификатор загрузчика
	private ProductAdapter adapter;
	private int positionItem;

	public static String TOTAL_PRICE = "TotalPrice";
	public static String CHECK_LIST_NAME = "CheckListName";

	private String barcode;
	private Button checkButtonSubmit;

	private PurchaseDialogFragment purchaseDialogFragment = new PurchaseDialogFragment();

	private Menu menu;

	private ImageView refreshImageView;
	private Animation rotateAnimation;
	private boolean isInRefresh = false;

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
		final SwipeDetector swipeDetector = new SwipeDetector();

		checkList = (ListView) findViewById(R.id.activity_check_listview);
		checkList.setOnTouchListener(swipeDetector);
		checkList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// Если был обнаружен свайп, то удаляем айтем
				if (swipeDetector.swipeDetected()) {
					float toX;
					if (swipeDetector.getAction() == SwipeDetector.Action.LR ||
							swipeDetector.getAction() == SwipeDetector.Action.RL) {
						positionItem = position;
						toX = swipeDetector.getAction() == SwipeDetector.Action.LR ? 1 : 0;
						view.startAnimation(getDeleteAnimation(0, (toX == 0) ? -view.getWidth() : 2 * view.getWidth()));
					}
				}
				// Иначе выбираем айтем
				else {

				}
			}
		});
		checkList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
				positionItem = i;
				showNoticeDialog();
				return true;
			}
		});

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

		final boolean needRefresh = getIntent().getBooleanExtra(LoginActivity.KEY_REFRESH, false);
		if (needRefresh) {
			syncProducts();
		}

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

		this.menu = menu;

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean out = super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
			case R.id.refresh:
				startRefreshAnimation();
				syncProducts();
				out = true;
				break;

			case R.id.add_product:
				Intent addProduct = new Intent(getApplicationContext(), PurchaseActivity.class);
				addProduct.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				startActivityForResult(addProduct, SELECT_PRODUCT);
				out = true;
				break;

			case R.id.scan_product:
				scanBarcode();
				out = true;
				break;
		}

		return out;
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

	private void startRefreshAnimation() {
		if (!isInRefresh) {
			isInRefresh = true;

			if (refreshImageView == null) {
				LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
				refreshImageView = (ImageView) inflater.inflate(R.layout.iv_refresh, null);
			}

			if (rotateAnimation == null) {
				rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh);
				rotateAnimation.setRepeatCount(Animation.INFINITE);
			}

			refreshImageView.startAnimation(rotateAnimation);

			MenuItem refreshMenuItem = menu.findItem(R.id.refresh);
			refreshMenuItem.setActionView(refreshImageView);
		}
	}

	private void stopRefreshAnimation() {
		if (isInRefresh) {
			isInRefresh = false;

			MenuItem refreshMenuItem = menu.findItem(R.id.refresh);

			if (refreshImageView != null) {
				refreshImageView.clearAnimation();
			}

			refreshMenuItem.setActionView(null);
		}
	}

	private void syncProducts() {
		new HttpsHelper.HttpsAsyncTask(
				SmartShopUrl.Shop.Item.URL_ITEM_LIST,
				null,
				productCallback,
				this
		).execute(HttpsHelper.Method.GET);
	}

	private HttpsHelper.HttpsAsyncTask.HttpsAsyncTaskCallback productCallback = new HttpsHelper.HttpsAsyncTask.HttpsAsyncTaskCallback() {
		@Override
		public void onPreExecute() {
			startRefreshAnimation();
		}

		@Override
		public void onPostExecute(JSONObject jsonObject) {
			try {
				if (isResponseSuccess(jsonObject.getInt(HttpsHelper.RESPONSE_CODE))) {
					DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());

					databaseHelper.dropTable(ContractClass.Products.TABLE_NAME);

					JSONArray products = jsonObject.getJSONArray(HttpsHelper.RESPONSE);
					for (int i = 0; i < products.length(); ++i) {
						JSONObject product = products.getJSONObject(i);

						String productName = product.getString("productName");
						String descriptionProduct = product.getString("descriptionProduct");
						String productBarcode = product.getString("productBarcode");
						String count = product.getString("count");
						String id = product.getString("id");
						String priceId = product.getString("price_id");
						String imageUrl = product.getString("image_url");

						JSONObject price = product.getJSONObject("price");
						String priceSellingProduct = price.getString("priceSellingProduct");
						String pricePurchaseProduct = price.getString("pricePurchaseProduct");

						ContentValues contentValues = new ContentValues();

						contentValues.put(ContractClass.Products.NAME, productName);
						contentValues.put(ContractClass.Products.DESCRIPTION, descriptionProduct);
						contentValues.put(ContractClass.Products.PRICE_SELLING, priceSellingProduct);
						contentValues.put(ContractClass.Products.PRICE_COST, pricePurchaseProduct);
						contentValues.put(ContractClass.Products.BARCODE, productBarcode);
						contentValues.put(ContractClass.Products._COUNT, count);
						contentValues.put(ContractClass.Products._ID, id);
						contentValues.put(ContractClass.Products.PRICE_ID, priceId);
						contentValues.put(ContractClass.Products.PHOTO_PATH, imageUrl);

						getContentResolver().insert(SmartShopContentProvider.PRODUCTS_CONTENT_URI, contentValues);
					}

					Toast.makeText(getApplicationContext(), "Товары успешно синхронизированы!", Toast.LENGTH_LONG)
							.show();
				}
			}
			catch (JSONException e) {
				e.printStackTrace();
			}

			stopRefreshAnimation();
		}

		@Override
		public void onCancelled() {
			stopRefreshAnimation();
		}
	};

	public void showNoticeDialog() {
		// Create an instance of the dialog fragment and show it
		DialogFragment dialog = new DeleteProductFromListDialogFragment();
		dialog.show(getSupportFragmentManager(), "NoticeDialogFragment");
	}

	@Override
	public void onDialogPositiveClick(DialogFragment dialog) {
		deleteProduct();
	}

	@Override
	public void onDialogNegativeClick(DialogFragment dialog) {

	}

	private void deleteProduct() {
		adapter.getListItems().remove(positionItem);
		adapter.notifyDataSetChanged();
		setTotalPrice(calculateSumCheck());
	}

	/**
	 * Запуск анимации удаления
	 */
	private Animation getDeleteAnimation(float fromX, float toX) {
		Animation animation = new TranslateAnimation(fromX, toX, 0, 0);
		animation.setStartOffset(100);
		animation.setDuration(800);
		animation.setAnimationListener(new DeleteAnimationListenter(CheckActivity.this));
		animation.setInterpolator(AnimationUtils.loadInterpolator(this,
				android.R.anim.anticipate_overshoot_interpolator));
		return animation;
	}

}
