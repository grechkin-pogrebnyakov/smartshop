package com.technopark.smartbiz.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.technopark.smartbiz.R;
import com.technopark.smartbiz.SmartShopPreferences;
import com.technopark.smartbiz.api.HttpsHelper;
import com.technopark.smartbiz.api.SmartShopUrl;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by titaevskiy.s on 03.12.15
 */
public class RegistrationIntentService extends IntentService {

	private static final String TAG = "RegIntentService";
	private static final String[] TOPICS = {"global"};

	public RegistrationIntentService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		try {
			InstanceID instanceID = InstanceID.getInstance(this);
			String token = instanceID.getToken(
					getString(R.string.gcm_defaultSenderId),
					GoogleCloudMessaging.INSTANCE_ID_SCOPE,
					null
			);
			Log.i(TAG, "GCM Registration Token: " + token);

			sendRegistrationToServer(token);

			subscribeTopics(token);

			// You should store a boolean that indicates whether the generated token has been
			// sent to your server. If the boolean is false, send the token to your server,
			// otherwise your server should have already received the token.
			sharedPreferences.edit().putBoolean(SmartShopPreferences.SENT_TOKEN_TO_SERVER, true).apply();
		}
		catch (Exception e) {
			Log.d(TAG, "Failed to complete token refresh", e);
			// If an exception happens while fetching the new token or updating our registration data
			// on a third-party server, this ensures that we'll attempt the update at a later time.
			sharedPreferences.edit().putBoolean(SmartShopPreferences.SENT_TOKEN_TO_SERVER, false).apply();
		}
		// Notify UI that registration has completed, so the progress indicator can be hidden.
		Intent registrationComplete = new Intent(SmartShopPreferences.REGISTRATION_COMPLETE);
		LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
	}

	/**
	 * Persist registration to third-party servers.
	 * <p/>
	 * Modify this method to associate the user's GCM registration token with any server-side account
	 * maintained by your application.
	 *
	 * @param token The new token.
	 */
	private void sendRegistrationToServer(String token) {
		JSONObject requestJsonObject = new JSONObject();
		try {
			requestJsonObject.put("gcm_registration_id", token);
		}
		catch (JSONException e) {
			e.printStackTrace();
		}

		new HttpsHelper.HttpsAsyncTask(SmartShopUrl.Profile.URL_REGISTER_DEVICE, requestJsonObject, null, getApplicationContext())
				.execute(HttpsHelper.Method.POST);
	}

	/**
	 * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
	 *
	 * @param token GCM token
	 * @throws IOException if unable to reach the GCM PubSub service
	 */
	private void subscribeTopics(String token) throws IOException {
		GcmPubSub pubSub = GcmPubSub.getInstance(this);
		for (String topic : TOPICS) {
			pubSub.subscribe(token, "/topics/" + topic, null);
		}
	}
}
