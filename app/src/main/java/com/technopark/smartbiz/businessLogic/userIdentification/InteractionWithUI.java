package com.technopark.smartbiz.businessLogic.userIdentification;

import org.json.JSONObject;

/**
 * Created by Abovyan on 27.11.15.
 */
public interface InteractionWithUI {
	void netActionResponse(int requestActionCode, JSONObject jsonResponse);

	void callbackAccessControl(int requestActionCode, String accessRightIdentificator);

	void showToast(String message);
}
