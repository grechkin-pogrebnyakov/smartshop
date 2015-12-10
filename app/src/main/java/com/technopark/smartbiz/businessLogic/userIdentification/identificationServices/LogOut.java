package com.technopark.smartbiz.businessLogic.userIdentification.identificationServices;

import android.content.Context;
import android.content.SharedPreferences;

import com.technopark.smartbiz.api.HttpsHelper;
import com.technopark.smartbiz.api.SmartShopUrl;
import com.technopark.smartbiz.businessLogic.userIdentification.InteractionWithUI;
import com.technopark.smartbiz.businessLogic.userIdentification.UserIdentificationContract;
import com.technopark.smartbiz.businessLogic.userIdentification.activities.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Abovyan on 10.12.15.
 */
public class LogOut implements HttpsHelper.HttpsAsyncTask.HttpsAsyncTaskCallback {
	private int requestActionCode;
	private Context context;

	private SharedPreferences sharedPreferences;
	private InteractionWithUI interactionWithUI;

	public LogOut(int requestActionCode, Context context, InteractionWithUI interactionWithUI) {
		this.requestActionCode = requestActionCode;
		this.context = context;
		this.interactionWithUI = interactionWithUI;
		sharedPreferences = context.getSharedPreferences(LoginActivity.APP_PREFERENCES, Context.MODE_PRIVATE);
	}

	public void startLogOut() {
		JSONObject logOutJsonObject = new JSONObject();
		new HttpsHelper.HttpsAsyncTask(SmartShopUrl.Auth.URL_LOGOUT, logOutJsonObject, this, context)
				.execute(HttpsHelper.Method.POST);
	}

	@Override
	public void onPreExecute() {

	}

	@Override
	public void onPostExecute(JSONObject jsonObject) {
		int logOutResult = logOut(jsonObject);
		try {
			if (jsonObject != null) {
				jsonObject.put(UserIdentificationContract.LOG_OUT_RESPONSE_STATUS_KEY, logOutResult);
			}
			else {
				interactionWithUI.showToast("Время ожидания истекло !");
				jsonObject = new JSONObject().put(UserIdentificationContract.LOG_OUT_RESPONSE_STATUS_KEY,
						UserIdentificationContract.LOGOUT_STATUS_FAIL);
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		interactionWithUI.netActionResponse(requestActionCode, jsonObject);
	}

	@Override
	public void onCancelled() {

	}

	private int logOut(JSONObject jsonResponce) {
		try {
			int responceCode = jsonResponce.getInt(HttpsHelper.RESPONSE_CODE);
			if (200 <= responceCode && responceCode < 300) {
				return UserIdentificationContract.LOGOUT_STATUS_SUCCESS;
			}else if (responceCode >= 500) {
				interactionWithUI.showToast("Ошибка сервера !");
				return UserIdentificationContract.LOGOUT_STATUS_FAIL;
			}
		}catch (JSONException e) {
			e.printStackTrace();
		}
//		interactionWithUI.showToast("Неизвестная ошибка !");
		return UserIdentificationContract.LOGOUT_STATUS_FAIL;
	}
}
