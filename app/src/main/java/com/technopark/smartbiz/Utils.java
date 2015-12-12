package com.technopark.smartbiz;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Base64;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Iterator;

/**
 * Created by titaevskiy.s on 26.11.15.
 */
public final class Utils {
	private Utils() {}

	public static JSONObject mergeJsonObject(JSONObject object1, JSONObject object2) {
		JSONObject mergeJsonObject = new JSONObject();

		try {
			mergeJsonObject = new JSONObject(object1.toString());

			Iterator it = object2.keys();
			while (it.hasNext()) {
				String key = (String) it.next();
				mergeJsonObject.accumulate(key, object2.get(key));
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
		}

		return mergeJsonObject;
	}

	public static ContentValues jsonToContentValues(JSONObject jsonObject, String[] names) {
		ContentValues contentValues = new ContentValues();

		for (String name : names) {
			if (jsonObject.has(name)) {
				try {
					contentValues.put(name, jsonObject.getString(name));
				}
				catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		return contentValues;
	}

	public static boolean isResponseSuccess(int responseCode) {
		return (responseCode >= 200 && responseCode < 300);
	}

	public static String imageToBase64String(String photoPath) {
		String base64String = "";
		File imageFile = new File(photoPath);

		if (imageFile.exists()) {
			Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());

			ByteArrayOutputStream bao = new ByteArrayOutputStream();
			bitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, true);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bao);
			byte[] bytes = bao.toByteArray();
			base64String = Base64.encodeToString(bytes, Base64.DEFAULT);
		}

		return base64String;
	}

	public static boolean isNetworkEnabled(Context context) {
		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		return (networkInfo != null && networkInfo.isConnected());
	}

	/**
	 * Показывает прогресс на пользовательском интерфейсе и закрывается после входа.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	public static void showProgress(final boolean show, final View formActionView, final View progressBarView, Context context) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = context.getResources().getInteger(android.R.integer.config_shortAnimTime);

			formActionView.setVisibility(show ? View.GONE : View.VISIBLE);
			formActionView.animate().setDuration(shortAnimTime).alpha(
					show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					formActionView.setVisibility(show ? View.GONE : View.VISIBLE);
				}
			});

			progressBarView.setVisibility(show ? View.VISIBLE : View.GONE);
			progressBarView.animate().setDuration(shortAnimTime).alpha(
					show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					progressBarView.setVisibility(show ? View.VISIBLE : View.GONE);
				}
			});
		}
		else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			progressBarView.setVisibility(show ? View.VISIBLE : View.GONE);
			formActionView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

}
