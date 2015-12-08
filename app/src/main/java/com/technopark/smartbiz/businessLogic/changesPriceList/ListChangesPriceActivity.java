package com.technopark.smartbiz.businessLogic.changesPriceList;

import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.technopark.smartbiz.R;
import com.technopark.smartbiz.api.HttpsHelper;
import com.technopark.smartbiz.api.SmartShopUrl;
import com.technopark.smartbiz.database.ContractClass;
import com.technopark.smartbiz.database.DatabaseHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.technopark.smartbiz.Utils.isResponseSuccess;


public class ListChangesPriceActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, SubmitChangePriceDialogFragment.NoticeDialogListener, HttpsHelper.HttpsAsyncTask.HttpsAsyncTaskCallback {

	private static final int LOADER_ID = 1;

	private SimpleCursorAdapter adapter;
	private long row_id;
	private long item_id;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_changes_price);

		final String[] from = new String[]{
				ContractClass.PriceUpdate.COLUMN_NAME_PRODUCT_NAME,
				ContractClass.PriceUpdate.COLUMN_NAME_OLD_PRICE,
				ContractClass.PriceUpdate.COLUMN_NAME_NEW_PRICE,
		};

		final int[] to = new int[]{
				R.id.item_change_price_name,
				R.id.item_change_price_old_price,
				R.id.item_change_price_new_price,
		};
		adapter = new SimpleCursorAdapter(this, R.layout.item_change_price, null, from, to, 0);

		ListView listView = (ListView) findViewById(R.id.activity_list_changes_price_listview);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				row_id = id;

				Cursor cursor = (Cursor) adapter.getItem(position);
				item_id = cursor.getLong(cursor.getColumnIndex(ContractClass.PriceUpdate.COLUMN_NAME_ITEM_ID));

				SubmitChangePriceDialogFragment dialogFragment = new SubmitChangePriceDialogFragment();
				dialogFragment.show(getFragmentManager(), "ChangePriceDialog");
			}
		});

		getLoaderManager().initLoader(LOADER_ID, null, this);

		new HttpsHelper.HttpsAsyncTask(SmartShopUrl.Shop.Item.URL_CHANGE_PRICE_LIST, null, this, this)
				.execute(HttpsHelper.Method.GET);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(
				this,
				ContractClass.PriceUpdate.CONTENT_URI,
				ContractClass.PriceUpdate.DEFAULT_PROJECTION,
				null,
				null,
				null
		);
	}

	@Override
	public void onLoadFinished(Loader loader, Cursor data) {
		adapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader loader) {
		adapter.swapCursor(null);
	}

	@Override
	public void onDialogPositiveClick(DialogFragment dialog) {
		submitChange();
	}

	private void submitChange() {
		getContentResolver().delete(
				Uri.withAppendedPath(ContractClass.PriceUpdate.CONTENT_URI, String.valueOf(row_id)),
				null,
				null
		);

		JSONObject requestJsonObject = new JSONObject();
		try {
			requestJsonObject.put("item_id", item_id);
		}
		catch (JSONException e) {
			e.printStackTrace();
		}

		new HttpsHelper.HttpsAsyncTask(SmartShopUrl.Shop.Item.URL_CONFIRM_PRICE_UPDATE, requestJsonObject, null, this)
				.execute(HttpsHelper.Method.POST);
	}

	@Override
	public void onPreExecute() {}

	@Override
	public void onPostExecute(JSONObject jsonObject) {
		try {
			if (isResponseSuccess(jsonObject.getInt(HttpsHelper.RESPONSE_CODE))) {
				DatabaseHelper databaseHelper = new DatabaseHelper(this);

				databaseHelper.dropTable(ContractClass.PriceUpdate.TABLE_NAME);

				JSONArray jsonArray = jsonObject.getJSONArray(HttpsHelper.RESPONSE);
				for (int i = 0; i < jsonArray.length(); ++i) {
					JSONObject updateObject = jsonArray.getJSONObject(i);

					String name = updateObject.getString("productName");
					String itemId = updateObject.getString("id");

					JSONObject oldPriceJsonObject = updateObject.getJSONObject("price");
					String oldPrice = oldPriceJsonObject.getString("priceSellingProduct");

					JSONObject newPriceJsonObject = updateObject.getJSONObject("new_price");
					String newPrice = newPriceJsonObject.getString("priceSellingProduct");

					ContentValues contentValues = new ContentValues();

					contentValues.put(ContractClass.PriceUpdate.COLUMN_NAME_PRODUCT_NAME, name);
					contentValues.put(ContractClass.PriceUpdate.COLUMN_NAME_ITEM_ID, itemId);
					contentValues.put(ContractClass.PriceUpdate.COLUMN_NAME_OLD_PRICE, oldPrice);
					contentValues.put(ContractClass.PriceUpdate.COLUMN_NAME_NEW_PRICE, newPrice);

					getContentResolver().insert(ContractClass.PriceUpdate.CONTENT_URI, contentValues);
				}
			}

			Toast.makeText(getApplicationContext(), "Изменения успешно синхронизированы!", Toast.LENGTH_LONG)
					.show();
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onCancelled() {}
}
