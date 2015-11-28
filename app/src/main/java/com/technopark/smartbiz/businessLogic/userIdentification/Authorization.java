package com.technopark.smartbiz.businessLogic.userIdentification;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.technopark.smartbiz.api.HttpsHelper;
import com.technopark.smartbiz.api.SmartShopUrl;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Abovyan on 26.11.15.
 */
public class Authorization implements HttpsHelper.HttpsAsyncTask.HttpsAsyncTaskCallback{

	private int requestActionCode;
	private Context context;

	private SharedPreferences sharedPreferences;
	private InteractionWithUI interactionWithUI;

	public Authorization(int requestActionCode, Context context, InteractionWithUI interactionWithUI) {
		this.requestActionCode = requestActionCode;
		this.context = context;
		this.interactionWithUI = interactionWithUI;
		sharedPreferences = context.getSharedPreferences(LoginActivity.APP_PREFERENCES, Context.MODE_PRIVATE);
	}

	public void startAuthorization(String username, String password) {
		JSONObject authorizationJsonObject = new JSONObject();
		try {
			authorizationJsonObject.accumulate(UserIdentificationContract.AUTHORIZATION_LOGIN_KEY, username);
			authorizationJsonObject.accumulate(UserIdentificationContract.AUTHORIZATION_PASSWORD_KEY, password);
			new HttpsHelper.HttpsAsyncTask(SmartShopUrl.Auth.URL_LOGIN, authorizationJsonObject, this, context)
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
		int authorizationResult = authorization(jsonObject);
		try {
			if (jsonObject != null) {
				jsonObject.put(UserIdentificationContract.AUTHORIZATION_RESPONSE_STATUS_KEY, authorizationResult);
			} else {
				interactionWithUI.showToast("Время ожидания истекло !");
				jsonObject = new JSONObject().put(UserIdentificationContract.AUTHORIZATION_RESPONSE_STATUS_KEY,
						UserIdentificationContract.AUTHORIZATION_STATUS_FAIL);
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		interactionWithUI.asynctaskActionResponse(requestActionCode, jsonObject);
	}

	private int authorization(JSONObject jsonResponce) {

		try {
			int responceCode = jsonResponce.getInt(HttpsHelper.RESPONSE_CODE);

			if (200 <= responceCode && responceCode < 300) {
				String token = jsonResponce.getString("key");
				if (!jsonResponce.has("default_password") || !jsonResponce.getBoolean("default_password")) {
					Log.e("cookie", token);
					sharedPreferences.edit().putString(UserIdentificationContract.TOKEN_AUTHORIZATION, token).commit();
					sharedPreferences.edit().putString(UserIdentificationContract.STATUS_AUTHORIZATION_KEY,
							UserIdentificationContract.SUCCESS_AUTHORIZATION).commit();
					Log.e("session", sharedPreferences.getString(UserIdentificationContract.TOKEN_AUTHORIZATION, "default"));
					interactionWithUI.showToast("Успешный вход");
					return UserIdentificationContract.AUTHORIZATION_STATUS_SUCCESS;
				}
				else {
					sharedPreferences.edit().putString(UserIdentificationContract.TOKEN_AUTHORIZATION, token).commit();
					return UserIdentificationContract.AUTHORIZATION_STATUS_CHANGE_PASSWORD;
				}
			}
			else if (300 <= responceCode && responceCode < 400) {
				interactionWithUI.showToast("Ошибка авторизации !");
				return UserIdentificationContract.AUTHORIZATION_STATUS_FAIL;
			}
			else if (400 <= responceCode && responceCode < 500) {
				interactionWithUI.showToast("Неправильный логин или пароль !");
				return UserIdentificationContract.AUTHORIZATION_STATUS_FAIL;
			}
			else if (responceCode >= 500) {
				interactionWithUI.showToast("Ошибка сервера !");
				return UserIdentificationContract.AUTHORIZATION_STATUS_FAIL;
			}

		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		interactionWithUI.showToast("Неизвестная ошибка !");
		return UserIdentificationContract.AUTHORIZATION_STATUS_FAIL;
	}
}
