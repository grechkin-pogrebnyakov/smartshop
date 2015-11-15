package com.technopark.smartbiz.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by Abovyan on 18.10.15.
 */
public class SmartShopContentProvider extends ContentProvider {

    public final String LOG_TAG = "DatabaseLog";

    // Таблица
    static final String PRODUCTS_TABLE = DatabaseHelper.PRODUCTS_TABLE_NAME;
    static final String CHECKS_TABLE = DatabaseHelper.CHECKS_TABLE_NAME;

    // Поля
    static final String ITEMS_ID = "_id";

    // // Uri
    // authority
    static final String AUTHORITY = "ru.tech_mail.smart_biz.data";

    // path
    static final String PRODUCTS_PATH = DatabaseHelper.PRODUCTS_TABLE_NAME;
    static final String CHECKS_PATH = DatabaseHelper.CHECKS_TABLE_NAME;

    // Общий Uri
    public static final Uri PRODUCTS_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + PRODUCTS_PATH);
    public static final Uri CHECKS_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + CHECKS_PATH);

    // Типы данных
    // набор строк
    static final String PRODUCTS_CONTENT_TYPE = "vnd.android.cursor.dir/vnd."
            + AUTHORITY + "." + PRODUCTS_PATH;
    static final String CHECKS_CONTENT_TYPE = "vnd.android.cursor.dir/vnd."
            + AUTHORITY + "." + PRODUCTS_PATH;

    // одна строка
    static final String PRODUCTS_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd."
            + AUTHORITY + "." + PRODUCTS_PATH;
    static final String CHECKS_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd."
            + AUTHORITY + "." + CHECKS_PATH;

    //// UriMatcher
    // общий Uri
    static final int URI_PRODUCTS = 1;
    static final int URI_CHECKS = 2;

    // Uri с указанным ID
    static final int URI_PRODUCTS_ID = 3;
    static final int URI_CHECKS_ID = 4;

    // описание и создание UriMatcher
    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, PRODUCTS_PATH, URI_PRODUCTS);
        uriMatcher.addURI(AUTHORITY, PRODUCTS_PATH + "/#", URI_PRODUCTS_ID);
        uriMatcher.addURI(AUTHORITY, CHECKS_PATH, URI_CHECKS);
        uriMatcher.addURI(AUTHORITY, CHECKS_PATH + "/#", URI_CHECKS_ID);
    }

    DatabaseHelper dbHelper;
    SQLiteDatabase db;


    @Override
    public boolean onCreate() {
        Log.d(LOG_TAG, "onCreate");
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Log.d(LOG_TAG, "query, " + uri.toString());
        String id;
        Cursor cursor;

        db = dbHelper.getWritableDatabase();

        // проверяем Uri
        switch (uriMatcher.match(uri)) {

            case URI_PRODUCTS: // общий Uri products
                Log.d(LOG_TAG, "URI_PRODUCTS");
                // если сортировка не указана, ставим свою - по имени
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = "name" + " ASC";
                }
                cursor = db.query(DatabaseHelper.PRODUCTS_TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(),
                        PRODUCTS_CONTENT_URI);
                break;

            case URI_PRODUCTS_ID: // Uri с ID products
                id = uri.getLastPathSegment();
                Log.d(LOG_TAG, "URI_PRODUCTS_ID, " + id);
                // добавляем ID к условию выборки
                if (TextUtils.isEmpty(selection)) {
                    selection = ITEMS_ID + " = " + id;
                } else {
                    selection = selection + " AND " + ITEMS_ID + " = " + id;
                }
                cursor = db.query(DatabaseHelper.PRODUCTS_TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(),
                        PRODUCTS_CONTENT_URI);
                break;

            case URI_CHECKS: // общий Uri checks
                Log.d(LOG_TAG, "URI_CHECKS");
                // если сортировка не указана, ставим свою - по имени
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = "name" + " ASC";
                }
                cursor = db.query(DatabaseHelper.CHECKS_TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(),
                        CHECKS_CONTENT_URI);
                break;

            case URI_CHECKS_ID: // Uri с ID checks
                id = uri.getLastPathSegment();
                Log.d(LOG_TAG, "URI_CHECKS_ID, " + id);
                // добавляем ID к условию выборки
                if (TextUtils.isEmpty(selection)) {
                    selection = ITEMS_ID + " = " + id;
                } else {
                    selection = selection + " AND " + ITEMS_ID + " = " + id;
                }
                cursor = db.query(DatabaseHelper.CHECKS_TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(),
                        CHECKS_CONTENT_URI);
                break;

            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }


        // просим ContentResolver уведомлять этот курсор
        // об изменениях данных в PRODUCTS_CONTENT_URI
//        getContext().getContentResolver().notifyChange(uri, null);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        Log.d(LOG_TAG, "getType, " + uri.toString());
        switch (uriMatcher.match(uri)) {
            case URI_PRODUCTS:
                return PRODUCTS_CONTENT_TYPE;
            case URI_PRODUCTS_ID:
                return PRODUCTS_CONTENT_ITEM_TYPE;

            case URI_CHECKS:
                return CHECKS_CONTENT_TYPE;
            case URI_CHECKS_ID:
                return CHECKS_CONTENT_ITEM_TYPE;
        }
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(LOG_TAG, "insert, " + uri.toString());
        long rowID;
        Uri resultUri = uri;
        db = dbHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {
            case URI_PRODUCTS:
                rowID = db.insert(DatabaseHelper.PRODUCTS_TABLE_NAME, null, values);
                resultUri = ContentUris.withAppendedId(PRODUCTS_CONTENT_URI, rowID);
                break;

            case URI_CHECKS:
                rowID = db.insert(DatabaseHelper.CHECKS_TABLE_NAME, null, values);
                resultUri = ContentUris.withAppendedId(CHECKS_CONTENT_URI, rowID);
                break;

            default: throw new IllegalArgumentException("Wrong URI: " + uri);
        }

        // уведомляем ContentResolver, что данные по адресу resultUri изменились
        getContext().getContentResolver().notifyChange(resultUri, null);
        return resultUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(LOG_TAG, "delete, " + uri.toString());
        int cnt;
        db = dbHelper.getWritableDatabase();
        String id;

        switch (uriMatcher.match(uri)) {
            case URI_PRODUCTS:
                Log.d(LOG_TAG, "URI_PRODUCTS_DELETE");
                cnt = db.delete(PRODUCTS_TABLE, selection, selectionArgs);
                break;
            case URI_PRODUCTS_ID:
                id = uri.getLastPathSegment();
                Log.d(LOG_TAG, "URI_PRODUCTS_ID_DELETE, " + id);
                if (TextUtils.isEmpty(selection)) {
                    selection = ITEMS_ID + " = " + id;
                } else {
                    selection = selection + " AND " + ITEMS_ID + " = " + id;
                }
                cnt = db.delete(PRODUCTS_TABLE, selection, selectionArgs);
                break;

            case URI_CHECKS:
                Log.d(LOG_TAG, "URI_CHECKS_DELETE");
                cnt = db.delete(CHECKS_TABLE, selection, selectionArgs);
                break;
            case URI_CHECKS_ID:
                id = uri.getLastPathSegment();
                Log.d(LOG_TAG, "URI_CHECKS_ID_DELETE, " + id);
                if (TextUtils.isEmpty(selection)) {
                    selection = ITEMS_ID + " = " + id;
                } else {
                    selection = selection + " AND " + ITEMS_ID + " = " + id;
                }
                cnt = db.delete(CHECKS_TABLE, selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }


        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        Log.d(LOG_TAG, "update, " + uri.toString());

        db = dbHelper.getWritableDatabase();
        int cnt;
        String id;

        switch (uriMatcher.match(uri)) {
            case URI_PRODUCTS:
                Log.d(LOG_TAG, "URI_PRODUCTS_UPDATE");
                cnt = db.update(PRODUCTS_TABLE, values, selection, selectionArgs);
                break;
            case URI_PRODUCTS_ID:
                id = uri.getLastPathSegment();
                Log.d(LOG_TAG, "URI_PRODUCTS_ID_UPDATE" + id);
                if (TextUtils.isEmpty(selection)) {
                    selection = ITEMS_ID + " = " + id;
                } else {
                    selection = selection + " AND " + ITEMS_ID + " = " + id;
                }
                cnt = db.update(PRODUCTS_TABLE, values, selection, selectionArgs);
                break;

            case URI_CHECKS:
                Log.d(LOG_TAG, "URI_CHECKS_UPDATE");
                cnt = db.update(CHECKS_TABLE, values, selection, selectionArgs);
                break;
            case URI_CHECKS_ID:
                id = uri.getLastPathSegment();
                Log.d(LOG_TAG, "URI_CHECKS_ID_UPDATE" + id);
                if (TextUtils.isEmpty(selection)) {
                    selection = ITEMS_ID + " = " + id;
                } else {
                    selection = selection + " AND " + ITEMS_ID + " = " + id;
                }
                cnt = db.update(CHECKS_TABLE, values, selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }


        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }
}
