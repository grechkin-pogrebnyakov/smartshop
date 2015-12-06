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

		public static final String COLUMN_NAME_FIRST_NAME = "first_name";
		public static final String COLUMN_NAME_LAST_NAME = "last_name";
		public static final String COLUMN_NAME_FATHER_NAME = "father_name";
		public static final String COLUMN_NAME_LOGIN = "login";

		public static final String TEMPORARY_PASSWORD = "temporary_password";

		public static final String DEFAULT_SORT_ORDER = COLUMN_NAME_LAST_NAME + " ASC";

		public static final String[] DEFAULT_PROJECTION = new String[]{
				Employees._ID,
				Employees.COLUMN_NAME_FIRST_NAME,
				Employees.COLUMN_NAME_LAST_NAME,
				Employees.COLUMN_NAME_FATHER_NAME,
				Employees.COLUMN_NAME_LOGIN,
		};

		private Employees() {}
	}

	public static final class Products implements BaseColumns {
		public static final String TABLE_NAME = "products";
		public static final String BARCODE = "barcode";
		public static final String PHOTO_PATH = "photo_path";
		public static final String NAME = "name";
		public static final String PRICE_SELLING = "price_selling_product";
		public static final String PRICE_COST = "price_cost";
		public static final String DESCRIPTION = "description";
		public static final String PRICE_ID = "price_id";

		private Products() {}
	}

	public static final class Сhecks implements BaseColumns {
		public static final String TABLE_NAME = "checks";
		public static final String ID_FROM_PRODUCTS_TABLE = "id_from_products_table";
		public static final String PHOTO_PATH = "photo_path";
		public static final String NAME = "name";
		public static final String PRICE_SELLING = "price_selling_product";
		public static final String PRICE_COST = "price_cost";
		public static final String DATE_TIME = "date_time";
		public static final String PRICE_ID = "price_id";
	}

	public static final class PriceUpdate implements BaseColumns {

		public static final String TABLE_NAME = "price_update";

		private static final String SCHEME = "content://";
		public static final String PATH_PRICE_UPDATE = "/" + TABLE_NAME;
		private static final String PATH_PRICE_UPDATE_ID = PATH_PRICE_UPDATE + "/";

		public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_PRICE_UPDATE);
		public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_PRICE_UPDATE_ID);
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + TABLE_NAME;
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + TABLE_NAME;

		public static final String COLUMN_NAME_ITEM_ID = "item_id";
		public static final String COLUMN_NAME_PRODUCT_NAME = "name";
		public static final String COLUMN_NAME_OLD_PRICE = "old_price";
		public static final String COLUMN_NAME_NEW_PRICE = "new_price";

		public static final String DEFAULT_SORT_ORDER = COLUMN_NAME_PRODUCT_NAME + " ASC";

		public static final String[] DEFAULT_PROJECTION = new String[]{
				PriceUpdate._ID,
				PriceUpdate.COLUMN_NAME_ITEM_ID,
				PriceUpdate.COLUMN_NAME_PRODUCT_NAME,
				PriceUpdate.COLUMN_NAME_OLD_PRICE,
				PriceUpdate.COLUMN_NAME_NEW_PRICE,
		};

		private PriceUpdate() {}
	}
}
