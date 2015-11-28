package com.technopark.smartbiz.businessLogic.userIdentification;

/**
 * Created by Abovyan on 27.11.15.
 */
public final class UserIdentificationContract {

	public static final String TOKEN_AUTORIZATION = "token";
	public static final int REQUEST_CODE_REGISTRATION_ACTION = 1;
	public static final int REQUEST_CODE_AUTHORIZATION_ACTION = 1;

	public final static String REGISTRATION_LOGIN_KEY = "username";
	public final static String REGISTRATION_PASSWORD1_KEY = "password1";
	public final static String REGISTRATION_PASSWORD2_KEY = "password2";
	public final static String REGISTRATION_RESPONCE_STATUS_KEY = "regResponceStatus";

	public final static int REGISTRATION_STATUS_SUCCESS = 1;
	public final static int REGISTRATION_STATUS_FAIL = 2;

	public final static int AUTHORIZATION_STATUS_SUCCESS = 3;
	public final static int AUTHORIZATION_STATUS_FAIL = 4;
	public final static int AUTHORIZATION_STATUS_CHANGE_PASSWORD = 5;

	public final static int CHANGE_PASSWORD_STATUS_SUCCESS = 6;
	public final static int CHANGE_PASSWORD_STATUS_FAIL = 6;
}
