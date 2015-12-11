package com.technopark.smartbiz.businessLogic.productSales;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.technopark.smartbiz.R;
import com.technopark.smartbiz.adapters.ProductAdapter;
import com.technopark.smartbiz.businessLogic.showProducts.EndlessScrollListener;
import com.technopark.smartbiz.database.ContractClass;
import com.technopark.smartbiz.database.items.Check;
import com.technopark.smartbiz.database.items.Product;

public class PurchaseActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

	private String DIALOG = "purchaseDialogFragment";
	public static String KEY_RESPONCE_OBJECT = "check";

	private ListView listViewAddedProducts;
	private final String LOG_TAG = "ListAddedProducts";
	// Уникальный идентификатор загрузчика
	private static final int LOADER_ID = 1;
	private ProductAdapter adapter;
	// The callbacks through which we will interact with the LoaderManager.
	private LoaderManager.LoaderCallbacks<Cursor> mCallbacks;
	final Uri CONTENT_URI = Uri
			.parse("content://ru.tech_mail.smart_biz.data/products");

	private PurchaseDialogFragment purchaseDialogFragment = new PurchaseDialogFragment();

	private DialogFragmentCallback dialogListener = new DialogFragmentCallback() {
		@Override
		public void callback() {
			Intent result = new Intent();
			Check check = purchaseDialogFragment.getCheck();
			check.setCount(purchaseDialogFragment.getProductCount());
			check.setPriceSellingProduct(purchaseDialogFragment.getProductPrice());
			result.putExtra(KEY_RESPONCE_OBJECT, check);
			// TODO Add data to result
			setResult(RESULT_OK, result);
			finish();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_purchase);

		mCallbacks = this;
		// Инициализируем загрузчик с идентификатором '1' и 'mCallbacks'.
		// Если загрузчик не существует, то он будет создан,
		// иначе он будет перезапущен.
		LoaderManager lm = getLoaderManager();
		lm.initLoader(LOADER_ID, null, mCallbacks);

		listViewAddedProducts = (ListView) findViewById(R.id.activity_purchase_listview);
		adapter = new ProductAdapter(this);
		listViewAddedProducts.setAdapter(adapter);
		listViewAddedProducts.setOnScrollListener(new EndlessScrollListener() {
			@Override
			public void loadData(int offset) {

			}
		});
		listViewAddedProducts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				Check check = ((Product) adapter.getListItems().get(i)).getCheck();
				showDialog(check);
			}
		});

		purchaseDialogFragment.setAddButtonCallback(dialogListener);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		// Создаем новый CursorLoader с нужными параметрами.
		return new CursorLoader(this.getApplicationContext(), CONTENT_URI,
				null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		// Если используется несколько загрузчиков, то удобнее через оператор switch-case
		switch (loader.getId()) {
			case LOADER_ID:
				// Данные загружены и готовы к использованию
				//simpleCursorAdapter.swapCursor( cursor );
				if (cursor.moveToFirst()) {
					do {
						String nameProduct = cursor.getString(cursor.getColumnIndex(ContractClass.Products.NAME));
						String descriptionProduct = cursor.getString(cursor
								.getColumnIndex(ContractClass.Products.DESCRIPTION));
						String photoPath = cursor.getString(cursor.getColumnIndex(ContractClass.Products.PHOTO_PATH));
						double priceSellingProduct = cursor.getDouble(cursor
								.getColumnIndex(ContractClass.Products.PRICE_SELLING));
						double pricePurchaseProduct = cursor.getDouble(cursor
								.getColumnIndex(ContractClass.Products.PRICE_COST));
						String productBarcode = cursor.getString(cursor.getColumnIndex(ContractClass.Products.BARCODE));
						int countProduct = cursor.getInt(cursor.getColumnIndex(ContractClass.Products._COUNT));
						long id = cursor.getLong(cursor.getColumnIndex(ContractClass.Products._ID));
						long priceId = cursor.getLong(cursor.getColumnIndex(ContractClass.Products.PRICE_ID));
						Product product = new Product(nameProduct, descriptionProduct, photoPath, priceSellingProduct,
								pricePurchaseProduct, productBarcode, countProduct, id, priceId);
						adapter.addItem(product);
					}
					while (cursor.moveToNext());
				}
				break;
		}
		adapter.notifyDataSetChanged();
		// список теперь содержит данные на экране
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// Если по какой-то причине данные не доступны, то удаляем ссылки на старые данные,
		// заменяя их пустым курсором
		//simpleCursorAdapter.swapCursor(null);
	}

	private void showDialog(Check check) {
		purchaseDialogFragment.setCheck(check);
		purchaseDialogFragment.setProductName(check.getProductName());
		purchaseDialogFragment.setProductPrice(check.getPriceSellingProduct());
		purchaseDialogFragment.setProductCount(1);
		purchaseDialogFragment.show(getFragmentManager(), DIALOG);
	}
}
