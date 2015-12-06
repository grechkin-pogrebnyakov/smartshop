package com.technopark.smartbiz.businessLogic.changesPriceList;

import android.app.DialogFragment;
import android.app.LoaderManager;
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

import com.technopark.smartbiz.R;
import com.technopark.smartbiz.database.ContractClass;


public class ListChangesPriceActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, SubmitChangePriceDialogFragment.NoticeDialogListener {

	private static final int LOADER_ID = 1;

	private SimpleCursorAdapter adapter;
	private long row_id;

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

				SubmitChangePriceDialogFragment dialogFragment = new SubmitChangePriceDialogFragment();
				dialogFragment.show(getFragmentManager(), "ChangePriceDialog");
			}
		});

		getLoaderManager().initLoader(LOADER_ID, null, this);
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

		// TODO Send to server
	}
}
