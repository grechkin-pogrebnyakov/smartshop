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
public class Registration implements HttpsHelper.HttpsAsyncTask.HttpsAsyncTaskCallback {

	private int requestActionCode;
	private Context context;

	private SharedPreferences sharedPreferences;
	private InteractionWithUI interactionWithUI;

	public Registration(int requestActionCode, Context context, InteractionWithUI interactionWithUI) {
		this.requestActionCode = requestActionCode;
		this.context = context;
		this.interactionWithUI = interactionWithUI;
		sharedPreferences = context.getSharedPreferences(LoginActivity.APP_PREFERENCES, Context.MODE_PRIVATE);
	}

	public void startRegistration(String username, String password1, String password2) {
		JSONObject registrationJsonObject = new JSONObject();
		try {
			registrationJsonObject.accumulate(UserIdentificationContract.REGISTRATION_LOGIN_KEY, username);
			registrationJsonObject.accumulate(UserIdentificationContract.REGISTRATION_PASSWORD1_KEY, password1);
			registrationJsonObject.accumulate(UserIdentificationContract.REGISTRATION_PASSWORD2_KEY, password2);
			new HttpsHelper.HttpsAsyncTask(SmartShopUrl.Auth.URL_REGISTRATION, registrationJsonObject, this, context)
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
		int registrationResult = registration(jsonObject);
		try {
			jsonObject.put(UserIdentificationContract.REGISTRATION_RESPONCE_STATUS_KEY, registrationResult);
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		interactionWithUI.asynctaskActionResponce(requestActionCode, jsonObject);
	}

	private int registration (JSONObject jsonResponce) {

		try {
			int responceCode = jsonResponce.getInt(HttpsHelper.RESPONSE_CODE);

			if (200 <= responceCode && responceCode < 300) {
				String token = jsonResponce.getString("key");
				Log.e("cookie", token);
				sharedPreferences.edit().putString(UserIdentificationContract.TOKEN_AUTORIZATION, token).commit();
				Log.e("session", sharedPreferences.getString(UserIdentificationContract.TOKEN_AUTORIZATION, "default"));
				interactionWithUI.showToast("Регистрация прошла успешно !");
				return UserIdentificationContract.REGISTRATION_STATUS_SUCCESS;
			}
			else if (300 <= responceCode && responceCode < 400) {
				interactionWithUI.showToast("Ошибка регистрации !");
				return UserIdentificationContract.REGISTRATION_STATUS_FAIL;
			}
			else if (400 <= responceCode && responceCode < 500) {
				interactionWithUI.showToast("Пользователь уже существует !");
				return UserIdentificationContract.REGISTRATION_STATUS_FAIL;
			}
			else if (responceCode >= 500) {
				interactionWithUI.showToast("Ошибка сервера !");
				return UserIdentificationContract.REGISTRATION_STATUS_FAIL;
			}

		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		interactionWithUI.showToast("Неизвестная ошибка !");
		return UserIdentificationContract.REGISTRATION_STATUS_FAIL;
	}
}
