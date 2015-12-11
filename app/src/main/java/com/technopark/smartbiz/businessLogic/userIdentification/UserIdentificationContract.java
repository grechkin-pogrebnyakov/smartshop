package com.technopark.smartbiz.businessLogic.userIdentification;

/**
 * Created by Abovyan on 27.11.15.
 */
public final class UserIdentificationContract {

	public static final String STATUS_AUTHORIZATION_KEY = "statusAuth";
	public static final String SUCCESS_AUTHORIZATION = "success";
	public static final String SUCCESS_AUTHORIZATION_OWNER = "success_owner";
	public static final String SUCCESS_AUTHORIZATION_EMPLOYEE = "success_employee";

	public static final String TOKEN_AUTHORIZATION = "token";
	public static final int REQUEST_CODE_REGISTRATION_ACTION = 1;
	public static final int REQUEST_CODE_AUTHORIZATION_ACTION = 2;
	public static final int REQUEST_CODE_CHANGE_PASSWORD_ACTION = 3;
	public static final int REQUEST_CODE_VK_AUTHORIZATION_ACTION = 4;
	public static final int REQUEST_CODE_LOG_OUT_ACTION = 5;


	public static final int REQUEST_CODE_ACCESS_LOGIN = 3;

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

	public static final String VK_AUTHORIZATION_EMAIL = "email";
	public static final String VK_AUTHORIZATION_ACCESS_TOKEN = "access_token";
	public static final String VK_AUTHORIZATION_USER_ID = "user_id";
	public static final String VK_AUTHORIZATION_RESPONSE_STATUS_KEY = "vkAuthResponceStatus";
	public static final int VK_AUTHORIZATION_STATUS_SUCCESS = 8;
	public static final int VK_AUTHORIZATION_STATUS_FAIL = 9;
	public static final String VK_AUTHORIZATION_FIRST_NAME = "first_name";
	public static final String VK_AUTHORIZATION_LAST_NAME = "last_name";
	public static final int VK_AUTHORIZATION_STATUS_CHANGE_PASSWORD = 10;

	public final static int LOGOUT_STATUS_SUCCESS = 11;
	public final static int LOGOUT_STATUS_FAIL = 12;
	public final static String LOG_OUT_RESPONSE_STATUS_KEY = "authResponceStatus";

	public enum Role {
		OWNER,
		SELLER,
		Error
	}

	private UserIdentificationContract() {}
}
