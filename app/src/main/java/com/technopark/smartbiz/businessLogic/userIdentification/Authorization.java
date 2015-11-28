package com.technopark.smartbiz.businessLogic.userIdentification;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.technopark.smartbiz.api.HttpsHelper;
import com.technopark.smartbiz.api.SmartShopUrl;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

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

	public void startAuthorization(String username, String password1, String password2) {
		JSONObject authorizationJsonObject = new JSONObject();
		try {
			authorizationJsonObject.accumulate(UserIdentificationContract.REGISTRATION_LOGIN_KEY, username);
			authorizationJsonObject.accumulate(UserIdentificationContract.REGISTRATION_PASSWORD1_KEY, password1);
			authorizationJsonObject.accumulate(UserIdentificationContract.REGISTRATION_PASSWORD2_KEY, password2);
			new HttpsHelper.HttpsAsyncTask(SmartShopUrl.Auth.URL_REGISTRATION, authorizationJsonObject, this, context)
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
		/*int authorizationResult = authorization(jsonObject);
		try {
			jsonObject.put(UserIdentificationContract.REGISTRATION_RESPONCE_STATUS_KEY, authorizationResult);
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		interactionWithUI.asynctaskActionResponce(requestActionCode, jsonObject);*/
	}

	/*private int authorization(JSONObject jsonResponce) {

		try {
			int responceCode = jsonResponce.getInt(HttpsHelper.RESPONSE_CODE);

			if (200 <= responceCode && responceCode < 300) {
				if (!jsonResponce.has("default_password") || jsonResponce.getInt("default_password") == 0) {
					String token = jsonResponce.getString("key");
					Log.e("cookie", token);
					sharedPreferences.edit().putString(UserIdentificationContract.TOKEN_AUTORIZATION, token).commit();
					Log.e("session", sharedPreferences.getString(UserIdentificationContract.TOKEN_AUTORIZATION, "default"));
					interactionWithUI.showToast("Успешный вход");
					return UserIdentificationContract.AUTHORIZATION_STATUS_SUCCESS;
				}
				else {
					//temporaryToken = jsonResponce.getString("key");
					return UserIdentificationContract.AUTHORIZATION_STATUS_CHANGE_PASSWORD;
				}
			}
			else if (300 <= responceCode && responceCode < 400) {
				interactionWithUI.showToast("Ошибка авторизации !");
				return "fail";
			}
			else if (400 <= responceCode && responceCode < 500) {
				interactionWithUI.showToast("Неправильный логин или пароль !");
				return "fail";
			}
			else if (responceCode >= 500) {
				interactionWithUI.showToast("Ошибка сервера !");
				return "fail";
			}

		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		interactionWithUI.showToast("Неизвестная ошибка !");
		return "fail";
	}*/
}
