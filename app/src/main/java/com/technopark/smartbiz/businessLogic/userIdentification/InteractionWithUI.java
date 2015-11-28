package com.technopark.smartbiz.businessLogic.userIdentification;

import org.json.JSONObject;

/**
 * Created by Abovyan on 27.11.15.
 */
public interface InteractionWithUI {
	public void asynctaskActionResponse(int requestActionCode, JSONObject jsonResponce);
	public void showToast(String message);
}
