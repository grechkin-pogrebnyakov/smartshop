package com.technopark.smartbiz.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Abovyan on 18.10.15.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

	public static final String PRODUCTS_TABLE_NAME = "products";
	public static final String CHECKS_TABLE_NAME = "checks";

	private static final String DATABASE_NAME = "smart_shop";
	private static final int DATABASE_VERSION = 1;

	private static final String LOG_DB = "DatabaseHelper";

	private static final String PRODUCT_TABLE_CREATE =
			"CREATE TABLE " + PRODUCTS_TABLE_NAME + " " +
					"(" +
					"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
					"barcode INTEGER UNIQUE, " +
					"photo_path TEXT, " +
					"name TEXT, " +
					"price_selling_product INTEGER, " +
					"price_cost_product INTEGER, " +
					"description TEXT, " +
					"count INTEGER" +
					");";

	private static final String CHECKS_TABLE_CREATE =
			"CREATE TABLE " + CHECKS_TABLE_NAME + " " +
					"(" +
					"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
					"id_from_products_table INTEGER, " +
					"photo_path TEXT, " +
					"name TEXT, " +
					"price_selling_product INTEGER, " +
					"price_cost_product INTEGER, " +
					"count INTEGER, " +
					"date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL" +
					");";

	private static final String EMPLOYEE_TABLE_CREATE =
			"CREATE TABLE " + ContractClass.Employees.TABLE_NAME + " " +
					"(" +
					ContractClass.Employees._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					ContractClass.Employees.COLUMN_NAME_FIRST_NAME + " TEXT NOT NULL, " +
					ContractClass.Employees.COLUMN_NAME_LAST_NAME + " TEXT NOT NULL, " +
					ContractClass.Employees.COLUMN_NAME_FATHER_NAME + " TEXT, " +
					ContractClass.Employees.COLUMN_NAME_LOGIN + " TEXT NOT NULL " +
					")";

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(LOG_DB, PRODUCT_TABLE_CREATE);
		Log.d(LOG_DB, CHECKS_TABLE_CREATE);
		Log.d(LOG_DB, EMPLOYEE_TABLE_CREATE);

		db.execSQL(PRODUCT_TABLE_CREATE);
		db.execSQL(CHECKS_TABLE_CREATE);
		db.execSQL(EMPLOYEE_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
	}

	public void dropTable(String table) {
		Log.d(LOG_DB, "Drop table " + table);

		SQLiteDatabase db = getWritableDatabase();

		switch (table) {
			case PRODUCTS_TABLE_NAME:
				db.execSQL("DROP TABLE IF EXISTS '" + table + "';");
				db.execSQL(PRODUCT_TABLE_CREATE);
				break;
		}

		db.close();
	}
}
