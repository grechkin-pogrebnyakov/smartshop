package com.technopark.smartbiz.database;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by titaevskiy.s on 24.11.15.
 */
public final class ContractClass {
	public static final String AUTHORITY = "ru.tech_mail.smart_biz.data";

	private ContractClass() {}

	public static final class Employees implements BaseColumns {
		public static final String TABLE_NAME = "employees";

		private static final String SCHEME = "content://";
		public static final String PATH_EMPLOYEES = "/" + TABLE_NAME;
		private static final String PATH_EMPLOYEES_ID = PATH_EMPLOYEES + "/";

		public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_EMPLOYEES);
		public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_EMPLOYEES_ID);
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + TABLE_NAME;
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + TABLE_NAME;

		public static final String COLUMN_NAME_NAME = "name";
		public static final String COLUMN_NAME_SURNAME = "surname";
		public static final String COLUMN_NAME_LOGIN = "login";

		public static final String DEFAULT_SORT_ORDER = COLUMN_NAME_SURNAME + " ASC";

		public static final String[] DEFAULT_PROJECTION = new String[]{
				Employees._ID,
				Employees.COLUMN_NAME_NAME,
				Employees.COLUMN_NAME_SURNAME,
				Employees.COLUMN_NAME_LOGIN,
		};

		private Employees() {}
	}
}
