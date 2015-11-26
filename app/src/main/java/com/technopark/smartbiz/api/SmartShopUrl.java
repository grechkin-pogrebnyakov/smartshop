package com.technopark.smartbiz.api;

/**
 * Created by titaevskiy.s on 24.11.15.
 */
public final class SmartShopUrl {

	private static final String SCHEME = "https://";
	private static final String HOST = "smartshop1.ddns.net";
	private static final String URL_HOST = SCHEME + HOST;

	private static final String API = "/api";
	private static final String URL_API = URL_HOST + API;

	private SmartShopUrl() {}

	public static final class Auth {

		private static final String AUTH = "/auth";

		private static final String LOGIN = "/login/";
		private static final String LOGOUT = "/logout/";
		private static final String REGISTRATION = "/registration/";

		private static final String URL_AUTH = URL_API + AUTH;

		public static final String URL_LOGIN = URL_AUTH + LOGIN;
		public static final String URL_LOGOUT = URL_AUTH + LOGOUT;
		public static final String URL_REGISTRATION = URL_AUTH + REGISTRATION;

		private Auth() {}
	}

	public static final class Employee {

		private static final String EMPLOYEE = "/employee";

		private static final String REGISTRATION = "/register/";
		private static final String LIST = "/list/";

		private static final String URL_EMPLOYEE = URL_API + EMPLOYEE;

		public static final String URL_EMPLOYEE_REGISTRATION = URL_EMPLOYEE + REGISTRATION;
		public static final String URL_EMPLOYEE_LIST = URL_EMPLOYEE + LIST;

		private Employee() {}
	}
}
