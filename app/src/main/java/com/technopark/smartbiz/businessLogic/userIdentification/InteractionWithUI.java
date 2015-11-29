package com.technopark.smartbiz.businessLogic.userIdentification;

import org.json.JSONObject;

/**
 * Created by Abovyan on 27.11.15.
 */
public interface InteractionWithUI {
	public void netActionResponse(int requestActionCode, JSONObject jsonResponce);
	public void callbackAccessControl(int requestActionCode, String accessRightIdentificator);
	public void showToast(String message);
}
