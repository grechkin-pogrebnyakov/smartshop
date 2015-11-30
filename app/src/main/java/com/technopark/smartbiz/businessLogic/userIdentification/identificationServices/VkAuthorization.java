package com.technopark.smartbiz.businessLogic.userIdentification.identificationServices;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.technopark.smartbiz.api.HttpsHelper;
import com.technopark.smartbiz.api.SmartShopUrl;
import com.technopark.smartbiz.businessLogic.userIdentification.InteractionWithUI;
import com.technopark.smartbiz.businessLogic.userIdentification.UserIdentificationContract;
import com.technopark.smartbiz.businessLogic.userIdentification.activities.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Abovyan on 29.11.15.
 */
public class VkAuthorization implements HttpsHelper.HttpsAsyncTask.HttpsAsyncTaskCallback{

	private int requestActionCode;
	private Context context;

	private SharedPreferences sharedPreferences;
	private InteractionWithUI interactionWithUI;

	public VkAuthorization(int requestActionCode, Context context, InteractionWithUI interactionWithUI) {
		this.requestActionCode = requestActionCode;
		this.context = context;
		this.interactionWithUI = interactionWithUI;
		sharedPreferences = context.getSharedPreferences(LoginActivity.APP_PREFERENCES, Context.MODE_PRIVATE);
	}

	public void startAuthorization(String token, String email, String userID, String firstName, String lastName) {
		JSONObject authorizationJsonObject = new JSONObject();
		try {
			authorizationJsonObject.accumulate(UserIdentificationContract.VK_AUTHORIZATION_EMAIL, email);
			authorizationJsonObject.accumulate(UserIdentificationContract.VK_AUTHORIZATION_ACCESS_TOKEN, token);
			authorizationJsonObject.accumulate(UserIdentificationContract.VK_AUTHORIZATION_USER_ID, userID);
			authorizationJsonObject.accumulate(UserIdentificationContract.VK_AUTHORIZATION_FIRST_NAME, firstName);
			authorizationJsonObject.accumulate(UserIdentificationContract.VK_AUTHORIZATION_LAST_NAME, lastName);
			new HttpsHelper.HttpsAsyncTask(SmartShopUrl.Auth.URL_VK_LOGIN, authorizationJsonObject, this, context)
					.execute(HttpsHelper.Method.POST);
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
		int authorizationResult = vkAuthorization(jsonObject);
		try {
			if (jsonObject != null) {
				jsonObject.put(UserIdentificationContract.VK_AUTHORIZATION_RESPONSE_STATUS_KEY, authorizationResult);
			}
			else {
				interactionWithUI.showToast("Время ожидания истекло !");
				jsonObject = new JSONObject().put(UserIdentificationContract.VK_AUTHORIZATION_RESPONSE_STATUS_KEY,
						UserIdentificationContract.VK_AUTHORIZATION_STATUS_FAIL);
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		interactionWithUI.netActionResponse(requestActionCode, jsonObject);
	}

	@Override
	public void onCancelled() {
		int authorizationResult = UserIdentificationContract.VK_AUTHORIZATION_STATUS_FAIL;
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(UserIdentificationContract.VK_AUTHORIZATION_RESPONSE_STATUS_KEY, authorizationResult);
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		interactionWithUI.netActionResponse(requestActionCode, jsonObject);
	}

	private int vkAuthorization(JSONObject jsonResponce) {

		try {
			int responceCode = jsonResponce.getInt(HttpsHelper.RESPONSE_CODE);

			if (200 <= responceCode && responceCode < 300) {
				String token = jsonResponce.getString("key");
				if (!jsonResponce.has("default_password") || !jsonResponce.getBoolean("default_password")) {
					Log.e("cookie", token);
					if (jsonResponce.has("is_worker")) {
						boolean is_worker = jsonResponce.getBoolean("is_worker");
						String authorizationStatus = is_worker ? UserIdentificationContract.SUCCESS_AUTHORIZATION_EMPLOYEE :
								UserIdentificationContract.SUCCESS_AUTHORIZATION_OWNER;
						sharedPreferences.edit().putString(UserIdentificationContract.TOKEN_AUTHORIZATION, token).apply();
						sharedPreferences.edit().putString(UserIdentificationContract.STATUS_AUTHORIZATION_KEY,
								authorizationStatus).apply();
						Log.e("session", sharedPreferences.getString(UserIdentificationContract.TOKEN_AUTHORIZATION, "default"));
						interactionWithUI.showToast("Успешный вход");
						return UserIdentificationContract.VK_AUTHORIZATION_STATUS_SUCCESS;
					}
				}
				else {
					sharedPreferences.edit().putString(UserIdentificationContract.TOKEN_AUTHORIZATION, token).apply();
					return UserIdentificationContract.AUTHORIZATION_STATUS_CHANGE_PASSWORD;
				}
			}
			else if (300 <= responceCode && responceCode < 400) {
				interactionWithUI.showToast("Ошибка авторизации !");
				return UserIdentificationContract.VK_AUTHORIZATION_STATUS_FAIL;
			}
			else if (400 <= responceCode && responceCode < 500) {
				interactionWithUI.showToast("Неправильный логин или пароль !");
				return UserIdentificationContract.VK_AUTHORIZATION_STATUS_FAIL;
			}
			else if (responceCode >= 500) {
				interactionWithUI.showToast("Ошибка сервера !");
				return UserIdentificationContract.VK_AUTHORIZATION_STATUS_FAIL;
			}

		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		interactionWithUI.showToast("Неизвестная ошибка !");
		return UserIdentificationContract.VK_AUTHORIZATION_STATUS_FAIL;
	}
}
