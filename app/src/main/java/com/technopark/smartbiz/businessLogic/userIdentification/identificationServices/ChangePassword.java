package com.technopark.smartbiz.businessLogic.userIdentification.identificationServices;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.technopark.smartbiz.api.HttpsHelper;
import com.technopark.smartbiz.api.SmartShopUrl;
import com.technopark.smartbiz.businessLogic.userIdentification.InteractionWithUI;
import com.technopark.smartbiz.businessLogic.userIdentification.activities.LoginActivity;
import com.technopark.smartbiz.businessLogic.userIdentification.UserIdentificationContract;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Abovyan on 26.11.15.
 */
public class ChangePassword implements HttpsHelper.HttpsAsyncTask.HttpsAsyncTaskCallback {

	private int requestActionCode;
	private Context context;

	private SharedPreferences sharedPreferences;
	private InteractionWithUI interactionWithUI;

	public ChangePassword(int requestActionCode, Context context, InteractionWithUI interactionWithUI) {
		this.requestActionCode = requestActionCode;
		this.context = context;
		this.interactionWithUI = interactionWithUI;
		sharedPreferences = context.getSharedPreferences(LoginActivity.APP_PREFERENCES, Context.MODE_PRIVATE);
	}

	public void startChangePassword(String oldPassword, String newPassword1, String newPassword2) {
		JSONObject changePasswordJsonObject = new JSONObject();
		try {
			changePasswordJsonObject.accumulate(UserIdentificationContract.CHANGE_PASSWORD_OLD_PASSWORD_KEY, oldPassword);
			changePasswordJsonObject.accumulate(UserIdentificationContract.CHANGE_PASSWORD_PASSWORD1_KEY, newPassword1);
			changePasswordJsonObject.accumulate(UserIdentificationContract.CHANGE_PASSWORD_PASSWORD2_KEY, newPassword2);
			if (sharedPreferences.contains(UserIdentificationContract.TOKEN_AUTHORIZATION)) {
				new HttpsHelper.HttpsAsyncTask(SmartShopUrl.Auth.URL_CHANGE_PASSWORD, changePasswordJsonObject, this, context)
						.execute(HttpsHelper.Method.POST);
			}
			else {
				onPostExecute(new JSONObject().put(HttpsHelper.RESPONSE_CODE, 300));
			}

		}
		catch (JSONException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void onPreExecute() {

	}

	@Override
	public void onPostExecute(JSONObject jsonObject) {
		int changePasswordResult = setNewPassword(jsonObject);
		try {
			jsonObject.put(UserIdentificationContract.CHANGE_PASSWORD_RESPONSE_STATUS_KEY, changePasswordResult);
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		interactionWithUI.netActionResponse(requestActionCode, jsonObject);
	}

	@Override
	public void onCancelled() {
		int changePasswordResult = UserIdentificationContract.CHANGE_PASSWORD_STATUS_FAIL;
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(UserIdentificationContract.CHANGE_PASSWORD_RESPONSE_STATUS_KEY, changePasswordResult);
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		interactionWithUI.netActionResponse(requestActionCode, jsonObject);
	}

	private int setNewPassword(JSONObject jsonResponse) {

		try {
			int responceCode = jsonResponse.getInt(HttpsHelper.RESPONSE_CODE);

			if (200 <= responceCode && responceCode < 300) {
				String token = sharedPreferences.getString(UserIdentificationContract.TOKEN_AUTHORIZATION, "");
				Log.e("cookie", token);
				sharedPreferences.edit().putString(UserIdentificationContract.STATUS_AUTHORIZATION_KEY,
						UserIdentificationContract.SUCCESS_AUTHORIZATION_EMPLOYEE).apply();
				Log.e("changePassword", "success");
				interactionWithUI.showToast("Новый пароль успешно установлен !");

				return UserIdentificationContract.CHANGE_PASSWORD_STATUS_SUCCESS;
			}
			else if (300 <= responceCode && responceCode < 400) {
				interactionWithUI.showToast("Ошибка изменения пароля !");
				return UserIdentificationContract.CHANGE_PASSWORD_STATUS_FAIL;
			}
			else if (400 <= responceCode && responceCode < 500) {
				interactionWithUI.showToast("Недопустимый пароль !");
				return UserIdentificationContract.CHANGE_PASSWORD_STATUS_FAIL;
			}
			else if (responceCode >= 500) {
				interactionWithUI.showToast("Ошибка сервера !");
				return UserIdentificationContract.CHANGE_PASSWORD_STATUS_FAIL;
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		interactionWithUI.showToast("Неизвестная ошибка !");
		return UserIdentificationContract.CHANGE_PASSWORD_STATUS_FAIL;
	}
}
