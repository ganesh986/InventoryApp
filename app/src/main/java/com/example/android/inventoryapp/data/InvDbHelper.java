package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by matteo on 03/07/2017.
 */

public class InvDbHelper extends SQLiteOpenHelper {

    public static final String TAG = InvDbHelper.class.getSimpleName();

    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "inventory.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;


    public InvDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_INVENTORY = "CREATE TABLE " + InvContract.InvEntry.TABLE_NAME + " ("
                + InvContract.InvEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InvContract.InvEntry.COL_NAME + " TEXT NOT NULL, "
                + InvContract.InvEntry.COL_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + InvContract.InvEntry.COL_PRICE + " REAL NOT NULL DEFAULT 0.0, "
                + InvContract.InvEntry.COL_PICTURE + " TEXT NOT NULL DEFAULT 'No images', "
                + InvContract.InvEntry.COL_DESCRIPTION + " TEXT NOT NULL DEFAULT 'New Product ', "
                + InvContract.InvEntry.COL_ITEMS_SOLD + " INTEGER NOT NULL DEFAULT 0, "
                + InvContract.InvEntry.COL_SUPPLIER + " TEXT NOT NULL DEFAULT 'new' "
                + ");";

        db.execSQL(SQL_CREATE_INVENTORY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + InvContract.InvEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);

    }
}
