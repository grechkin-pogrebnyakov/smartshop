package com.technopark.smartbiz.businessLogic.showProducts;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.technopark.smartbiz.businessLogic.deleteProduct.DeleteProductFromListDialogFragment;
import com.technopark.smartbiz.businessLogic.editProduct.EditProductActivity;
import com.technopark.smartbiz.R;
import com.technopark.smartbiz.adapters.ProductAdapter;
import com.technopark.smartbiz.database.SmartShopContentProvider;
import com.technopark.smartbiz.database.items.Product;


public class ListAddedProducts extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
		DeleteProductFromListDialogFragment.NoticeDialogListener {

	private ListView listViewAddedProducts;
	private final String LOG_TAG = "ListAddedProducts";
	public static final String SEND_PRODUCT_NAME = "SendProductName";
	// Уникальный идентификатор загрузчика
	private static final int LOADER_ID = 1;
	private ProductAdapter adapter;
	private Product product;
	// The callbacks through which we will interact with the LoaderManager.
	private LoaderManager.LoaderCallbacks<Cursor> mCallbacks;
	final Uri CONTENT_URI = Uri
			.parse("content://ru.tech_mail.smart_biz.data/products");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_added_products);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		mCallbacks = this;
		// Инициализируем загрузчик с идентификатором '1' и 'mCallbacks'.
		// Если загрузчик не существует, то он будет создан,
		// иначе он будет перезапущен.
		LoaderManager lm = getLoaderManager();
		lm.initLoader(LOADER_ID, null, mCallbacks);

		listViewAddedProducts = (ListView) findViewById(R.id.clap_name_product_textView);
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
				Intent intent = new Intent(getApplicationContext(), EditProductActivity.class);
				intent.putExtra(SEND_PRODUCT_NAME, (Product) adapter.getListItems().get(i));
				startActivity(intent);
				finish();
			}
		});

		listViewAddedProducts.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
				product = (Product) adapter.getItem(i);
				showNoticeDialog();
				return true;
			}
		});
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
		adapter.getListItems().clear();
		switch (loader.getId()) {
			case LOADER_ID:
				// Данные загружены и готовы к использованию
				//simpleCursorAdapter.swapCursor( cursor );
				if (cursor.moveToFirst()) {
					do {
						String nameProduct = cursor.getString(cursor.getColumnIndex("name"));
						String descriptionProduct = cursor.getString(cursor.getColumnIndex("description"));
						String photoPath = cursor.getString(cursor.getColumnIndex("photo_path"));
						int priceSellingProduct = cursor.getInt(cursor.getColumnIndex("price_selling_product"));
						int pricePurchaseProduct = cursor.getInt(cursor.getColumnIndex("price_cost_product"));
						int productBarcode = cursor.getInt(cursor.getColumnIndex("barcode"));
						int countProduct = cursor.getInt(cursor.getColumnIndex("count"));
						long id = cursor.getInt(cursor.getColumnIndex("_id"));
						Product product = new Product(nameProduct, descriptionProduct, photoPath, priceSellingProduct,
								pricePurchaseProduct, productBarcode, countProduct, id);
						adapter.addItem(product);
					}
					while (cursor.moveToNext());
				}
				break;
		}
		// список теперь содержит данные на экране
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// Если по какой-то причине данные не доступны, то удаляем ссылки на старые данные,
		// заменяя их пустым курсором
		//simpleCursorAdapter.swapCursor(null);
	}

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
		String mSelectionClause = "_id = ?";
		String[] mSelectionArgs = {String.valueOf(product.getId())};
		getContentResolver().delete(SmartShopContentProvider.PRODUCTS_CONTENT_URI, mSelectionClause, mSelectionArgs );
	}
}
