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
    static final String ITEMS_TABLE = DatabaseHelper.PRODUCT_TABLE_NAME;

    // Поля
    static final String ITEMS_ID = "_id";

    // // Uri
    // authority
    static final String AUTHORITY = "ru.tech_mail.smart_biz.data";

    // path
    static final String ITEMS_PATH = DatabaseHelper.PRODUCT_TABLE_NAME;

    // Общий Uri
    public static final Uri ITEMS_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + ITEMS_PATH);

    // Типы данных
    // набор строк
    static final String ITEMS_CONTENT_TYPE = "vnd.android.cursor.dir/vnd."
            + AUTHORITY + "." + ITEMS_PATH;

    // одна строка
    static final String ITEMS_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd."
            + AUTHORITY + "." + ITEMS_PATH;

    //// UriMatcher
    // общий Uri
    static final int URI_ITEMS = 1;

    // Uri с указанным ID
    static final int URI_ITEMS_ID = 2;

    // описание и создание UriMatcher
    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, ITEMS_PATH, URI_ITEMS);
        uriMatcher.addURI(AUTHORITY, ITEMS_PATH + "/#", URI_ITEMS_ID);
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
        // проверяем Uri
        switch (uriMatcher.match(uri)) {
            case URI_ITEMS: // общий Uri
                Log.d(LOG_TAG, "URI_ITEMS");
                // если сортировка не указана, ставим свою - по имени
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = "name" + " ASC";
                }
                break;
            case URI_ITEMS_ID: // Uri с ID
                String id = uri.getLastPathSegment();
                Log.d(LOG_TAG, "URI_CONTACTS_ID, " + id);
                // добавляем ID к условию выборки
                if (TextUtils.isEmpty(selection)) {
                    selection = ITEMS_ID + " = " + id;
                } else {
                    selection = selection + " AND " + ITEMS_ID + " = " + id;
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(DatabaseHelper.PRODUCT_TABLE_NAME, projection, selection,
                selectionArgs, null, null, sortOrder);
        // просим ContentResolver уведомлять этот курсор
        // об изменениях данных в ITEMS_CONTENT_URI
        cursor.setNotificationUri(getContext().getContentResolver(),
                ITEMS_CONTENT_URI);
        getContext().getContentResolver().notifyChange(uri, null);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        Log.d(LOG_TAG, "getType, " + uri.toString());
        switch (uriMatcher.match(uri)) {
            case URI_ITEMS:
                return ITEMS_CONTENT_TYPE;
            case URI_ITEMS_ID:
                return ITEMS_CONTENT_ITEM_TYPE;
        }
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(LOG_TAG, "insert, " + uri.toString());
        if (uriMatcher.match(uri) != URI_ITEMS)
            throw new IllegalArgumentException("Wrong URI: " + uri);

        db = dbHelper.getWritableDatabase();
        long rowID = db.insert(DatabaseHelper.PRODUCT_TABLE_NAME, null, values);
        Uri resultUri = ContentUris.withAppendedId(ITEMS_CONTENT_URI, rowID);
        // уведомляем ContentResolver, что данные по адресу resultUri изменились
        getContext().getContentResolver().notifyChange(resultUri, null);
        return resultUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(LOG_TAG, "delete, " + uri.toString());
        switch (uriMatcher.match(uri)) {
            case URI_ITEMS:
                Log.d(LOG_TAG, "URI_CONTACTS");
                break;
            case URI_ITEMS_ID:
                String id = uri.getLastPathSegment();
                Log.d(LOG_TAG, "URI_CONTACTS_ID, " + id);
                if (TextUtils.isEmpty(selection)) {
                    selection = ITEMS_ID + " = " + id;
                } else {
                    selection = selection + " AND " + ITEMS_ID + " = " + id;
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        db = dbHelper.getWritableDatabase();
        int cnt = db.delete(ITEMS_TABLE, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        Log.d(LOG_TAG, "update, " + uri.toString());
        switch (uriMatcher.match(uri)) {
            case URI_ITEMS:
                Log.d(LOG_TAG, "URI_CONTACTS");

                break;
            case URI_ITEMS_ID:
                String id = uri.getLastPathSegment();
                Log.d(LOG_TAG, "URI_CONTACTS_ID, " + id);
                if (TextUtils.isEmpty(selection)) {
                    selection = ITEMS_ID + " = " + id;
                } else {
                    selection = selection + " AND " + ITEMS_ID + " = " + id;
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        db = dbHelper.getWritableDatabase();
        int cnt = db.update(ITEMS_TABLE, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }
}
