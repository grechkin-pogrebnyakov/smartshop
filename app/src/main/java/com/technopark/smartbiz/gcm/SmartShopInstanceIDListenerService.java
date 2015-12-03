package com.technopark.smartbiz.gcm;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by titaevskiy.s on 03.12.15
 */
public class SmartShopInstanceIDListenerService extends InstanceIDListenerService {

	private static final String TAG = "MyInstanceIDLS";

	/**
	 * Called if InstanceID token is updated. This may occur if the security of
	 * the previous token had been compromised. This call is initiated by the
	 * InstanceID provider.
	 */
	@Override
	public void onTokenRefresh() {
		// Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
		Intent intent = new Intent(this, RegistrationIntentService.class);
		startService(intent);
	}
}
