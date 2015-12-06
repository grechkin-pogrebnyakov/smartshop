package com.technopark.smartbiz.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Abovyan on 18.10.15.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "smart_shop";
	private static final int DATABASE_VERSION = 1;

	private static final String LOG_DB = "DatabaseHelper";

	private static final String PRODUCT_TABLE_CREATE =
			"CREATE TABLE " + ContractClass.Products.TABLE_NAME + " " +
					"(" +
					ContractClass.Products._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					ContractClass.Products.BARCODE + " TEXT UNIQUE, " +
					ContractClass.Products.PHOTO_PATH + " TEXT, " +
					ContractClass.Products.NAME + " TEXT, " +
					ContractClass.Products.PRICE_SELLING + " DOUBLE, " +
					ContractClass.Products.PRICE_COST + " DOUBLE, " +
					ContractClass.Products.DESCRIPTION + " TEXT, " +
					ContractClass.Products._COUNT + " INTEGER" +
					");";

	private static final String CHECKS_TABLE_CREATE =
			"CREATE TABLE " + ContractClass.Сhecks.TABLE_NAME + " " +
					"(" +
					ContractClass.Сhecks._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					ContractClass.Сhecks.ID_FROM_PRODUCTS_TABLE + " INTEGER, " +
					ContractClass.Сhecks.PHOTO_PATH + " TEXT, " +
					ContractClass.Сhecks.NAME + " TEXT, " +
					ContractClass.Сhecks.PRICE_SELLING + " DOUBLE, " +
					ContractClass.Сhecks.PRICE_COST + " DOUBLE, " +
					ContractClass.Сhecks._COUNT + " INTEGER, " +
					ContractClass.Сhecks.DATE_TIME + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL" +
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

	private static final String PRICE_UPDATE_TABLE_CREATE =
			"CREATE TABLE " + ContractClass.PriceUpdate.TABLE_NAME + " " +
					"(" +
					ContractClass.PriceUpdate._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					ContractClass.PriceUpdate.COLUMN_NAME_PRODUCT_ID + " INTEGER NOT NULL, " +
					ContractClass.PriceUpdate.COLUMN_NAME_PRODUCT_NAME + " TEXT NOT NULL, " +
					ContractClass.PriceUpdate.COLUMN_NAME_OLD_PRICE + " DOUBLE NOT NULL, " +
					ContractClass.PriceUpdate.COLUMN_NAME_NEW_PRICE + " DOUBLE NOT NULL" +
					")";

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(LOG_DB, PRODUCT_TABLE_CREATE);
		Log.d(LOG_DB, CHECKS_TABLE_CREATE);
		Log.d(LOG_DB, EMPLOYEE_TABLE_CREATE);
		Log.d(LOG_DB, PRICE_UPDATE_TABLE_CREATE);

		db.execSQL(PRODUCT_TABLE_CREATE);
		db.execSQL(CHECKS_TABLE_CREATE);
		db.execSQL(EMPLOYEE_TABLE_CREATE);
		db.execSQL(PRICE_UPDATE_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
	}

	public void dropTable(String table) {
		Log.d(LOG_DB, "Drop table " + table);

		SQLiteDatabase db = getWritableDatabase();

		switch (table) {
			case ContractClass.Products.TABLE_NAME:
				db.execSQL("DROP TABLE IF EXISTS '" + table + "';");
				db.execSQL(PRODUCT_TABLE_CREATE);
				break;
			case ContractClass.Сhecks.TABLE_NAME:
				db.execSQL("DROP TABLE IF EXISTS '" + table + "';");
				db.execSQL(CHECKS_TABLE_CREATE);
		}
		db.close();
	}
}
