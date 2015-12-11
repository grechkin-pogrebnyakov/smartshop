package com.technopark.smartbiz.businessLogic.userIdentification;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.technopark.smartbiz.Permission;
import com.technopark.smartbiz.businessLogic.userIdentification.activities.LoginActivity;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Abovyan on 29.11.15.
 */
public class AccessControl {

	private SharedPreferences sharedPreferences;
	private InteractionWithUI interactionWithUI;
	private int requestActionCode;

	private static final Map<UserIdentificationContract.Role, List<Permission>> rolePermissionsMap = new HashMap<UserIdentificationContract.Role, List<Permission>>() {{
		put(
				UserIdentificationContract.Role.OWNER,
				Arrays.asList(
						Permission.OWNER,
						Permission.SELLER
				)
		);
		put(
				UserIdentificationContract.Role.SELLER,
				Collections.singletonList(
						Permission.SELLER
				)
		);
	}};

	public AccessControl(Context context, InteractionWithUI interactionWithUI, int requestActionCode) {
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

	public static UserIdentificationContract.Role getCurrentUserRole(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(LoginActivity.APP_PREFERENCES, Context.MODE_PRIVATE);
		if (sharedPreferences.contains(UserIdentificationContract.STATUS_AUTHORIZATION_KEY)) {
			switch (sharedPreferences.getString(UserIdentificationContract.STATUS_AUTHORIZATION_KEY, "")) {
				case UserIdentificationContract.SUCCESS_AUTHORIZATION_OWNER:
					return UserIdentificationContract.Role.OWNER;
				case UserIdentificationContract.SUCCESS_AUTHORIZATION_EMPLOYEE:
					return UserIdentificationContract.Role.SELLER;
			}
		}
		return UserIdentificationContract.Role.Error;
	}

	public static List<Permission> getCurrentUserPermissions(Context context) {
		return rolePermissionsMap.get(getCurrentUserRole(context));
	}
}
