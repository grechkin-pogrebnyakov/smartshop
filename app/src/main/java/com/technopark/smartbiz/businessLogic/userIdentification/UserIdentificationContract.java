package com.technopark.smartbiz.businessLogic.userIdentification;

/**
 * Created by Abovyan on 27.11.15.
 */
public final class UserIdentificationContract {

	public static final String STATUS_AUTHORIZATION_KEY = "statusAuth";
	public static final String SUCCESS_AUTHORIZATION = "success";

	public static final String TOKEN_AUTHORIZATION = "token";
	public static final int REQUEST_CODE_REGISTRATION_ACTION = 1;
	public static final int REQUEST_CODE_AUTHORIZATION_ACTION = 2;
	public static final int REQUEST_CODE_CHANGE_PASSWORD_ACTION = 3;

	public final static String REGISTRATION_LOGIN_KEY = "username";
	public final static String REGISTRATION_PASSWORD1_KEY = "password1";
	public final static String REGISTRATION_PASSWORD2_KEY = "password2";
	public final static String REGISTRATION_RESPONSE_STATUS_KEY = "regResponceStatus";

	public final static int REGISTRATION_STATUS_SUCCESS = 1;
	public final static int REGISTRATION_STATUS_FAIL = 2;

	public final static String AUTHORIZATION_LOGIN_KEY = "username";
	public final static String AUTHORIZATION_PASSWORD_KEY = "password";

	public final static int AUTHORIZATION_STATUS_SUCCESS = 3;
	public final static int AUTHORIZATION_STATUS_FAIL = 4;
	public final static int AUTHORIZATION_STATUS_CHANGE_PASSWORD = 5;
	public final static String AUTHORIZATION_RESPONSE_STATUS_KEY = "authResponceStatus";

	public final static int CHANGE_PASSWORD_STATUS_SUCCESS = 6;
	public final static int CHANGE_PASSWORD_STATUS_FAIL = 7;
	public final static String CHANGE_PASSWORD_RESPONSE_STATUS_KEY = "authResponceStatus";
	public final static String CHANGE_PASSWORD_OLD_PASSWORD_KEY = "old_password";
	public final static String CHANGE_PASSWORD_PASSWORD1_KEY = "new_password1";
	public final static String CHANGE_PASSWORD_PASSWORD2_KEY = "new_password2";

}
