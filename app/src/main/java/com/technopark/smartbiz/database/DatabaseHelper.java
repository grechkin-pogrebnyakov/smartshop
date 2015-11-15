package com.technopark.smartbiz.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Abovyan on 18.10.15.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

	// // Константы для БД
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "smartShop";
	public static final String PRODUCTS_TABLE_NAME = "products";
	public static final String CHECKS_TABLE_NAME = "Checks";


	private static final String PRODUCT_TABLE_CREATE =
			"CREATE TABLE " + PRODUCTS_TABLE_NAME +
					" (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
					" barcode INTEGER UNIQUE," +
					" photo_path TEXT, " +
					" name TEXT, " +
					" price_selling_product INTEGER, " +
					" price_cost_product INTEGER, " +
					" description TEXT, " +
					" count INTEGER );";

	private static final String CHECKS_TABLE_CREATE =
			"CREATE TABLE " + CHECKS_TABLE_NAME +
					" (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
					" id_from_products_table INTEGER," +
					" photo_path TEXT, " +
					" name TEXT, " +
					" price_selling_product INTEGER, " +
					" price_cost_product INTEGER, " +
					" count INTEGER," +
					" date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL" +
					" );";

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(PRODUCT_TABLE_CREATE);
		db.execSQL(CHECKS_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

	}

}
