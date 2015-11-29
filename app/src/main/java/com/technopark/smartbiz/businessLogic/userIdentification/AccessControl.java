package com.technopark.smartbiz.businessLogic.userIdentification;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.technopark.smartbiz.businessLogic.userIdentification.activities.LoginActivity;

/**
 * Created by Abovyan on 29.11.15.
 */
public class AccessControl {

	private Context context;
	private SharedPreferences sharedPreferences;
	private InteractionWithUI interactionWithUI;
	private int requestActionCode;

	public AccessControl(Context context, InteractionWithUI interactionWithUI, int requestActionCode) {
		this.context = context;
		this.interactionWithUI = interactionWithUI;
		this.requestActionCode = requestActionCode;
		sharedPreferences = context.getSharedPreferences(LoginActivity.APP_PREFERENCES, Context.MODE_PRIVATE);
	}

	public void displayActivityOfAccessRights() {
		if (sharedPreferences.contains(UserIdentificationContract.STATUS_AUTHORIZATION_KEY)) {
			String statusAuthorization = sharedPreferences.getString(UserIdentificationContract.STATUS_AUTHORIZATION_KEY, "");
			Log.e("statusAuthorization", statusAuthorization);
			interactionWithUI.callbackAccessControl(requestActionCode, statusAuthorization);
		}
	}

}
