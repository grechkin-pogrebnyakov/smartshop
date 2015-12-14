package com.technopark.smartbiz.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmListenerService;
import com.technopark.smartbiz.HomeProxyActivity;
import com.technopark.smartbiz.R;
import com.technopark.smartbiz.api.HttpsHelper;
import com.technopark.smartbiz.api.SmartShopUrl;
import com.technopark.smartbiz.businessLogic.changesPriceList.ListChangesPriceActivity;
import com.technopark.smartbiz.database.ContractClass;
import com.technopark.smartbiz.database.DatabaseHelper;
import com.technopark.smartbiz.database.SmartShopContentProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.technopark.smartbiz.Utils.isResponseSuccess;

/**
 * Created by titaevskiy.s on 03.12.15
 */
public class SmartShopGcmListenerService extends GcmListenerService {

	private static final String TAG = "MyGcmListenerService";

	/**
	 * Called when message is received.
	 *
	 * @param from SenderID of the sender.
	 * @param data Data bundle containing message data as key/value pairs.
	 *             For Set of keys use data.keySet().
	 */
	@Override
	public void onMessageReceived(String from, Bundle data) {
		String message = data.getString("message");
		Log.d(TAG, "From: " + from);
		Log.d(TAG, "Message: " + message);

		String type = data.getString("type", "0");
		switch (type) {
			case "1":
				syncProducts();
				break;
		}
		Log.d(TAG, "Type: " + type);

		sendNotification(message, type);
	}

	/**
	 * Create and show a simple notification containing the received GCM message.
	 *
	 * @param message GCM message received.
	 * @param type
	 */
	private void sendNotification(String message, String type) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		switch (type) {
			case "2":
				intent.setClass(this, ListChangesPriceActivity.class);
				break;

			default:
				intent.setClass(this, HomeProxyActivity.class);
				break;
		}

		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);

		Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_assignment_white_24dp)
				.setContentTitle("SmartShop")
				.setContentText(message)
				.setAutoCancel(true)
				.setSound(defaultSoundUri)
				.setContentIntent(pendingIntent);

		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
	}

	private HttpsHelper.HttpsAsyncTask.HttpsAsyncTaskCallback productCallback = new HttpsHelper.HttpsAsyncTask.HttpsAsyncTaskCallback() {
		@Override
		public void onPreExecute() {}

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
		}

		@Override
		public void onCancelled() {}
	};

	private void syncProducts() {
		new HttpsHelper.HttpsAsyncTask(
				SmartShopUrl.Shop.Item.URL_ITEM_LIST,
				null,
				productCallback,
				this
		).execute(HttpsHelper.Method.GET);
	}
}
