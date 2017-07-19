package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.example.android.inventoryapp.R;

/**
 * Created by matteo on 03/07/2017.
 */

public class InvProvider  extends ContentProvider {

    public static final String TAG = InvProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the inventory table
     */
    private static final int INVENTORY = 100;

    /**
     * URI matcher code for the content URI for single item in the inventory table
     */
    private static final int INVENTORY_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {

        sUriMatcher.addURI(InvContract.CONTENT_AUTHORITY, InvContract.PATH_INVENTORY, INVENTORY);

        sUriMatcher.addURI(InvContract.CONTENT_AUTHORITY, InvContract.PATH_INVENTORY + "/#", INVENTORY_ID);
    }

    /**
     * Database helper object
     */
    private InvDbHelper mDbHelper;


    @Override
    public boolean onCreate() {
        mDbHelper = new InvDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri,
                        String[] projection,
                        String selection,
                        String[] selectionArgs,
                        String sortOrder) {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                cursor = db.query(InvContract.InvEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case INVENTORY_ID:

                selection = InvContract.InvEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = db.query(InvContract.InvEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return InvContract.InvEntry.CONTENT_LIST_TYPE;
            case INVENTORY_ID:
                return InvContract.InvEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }

    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return insertProduct(uri, values);
            default:
                throw new IllegalArgumentException(R.string.err_bad_uri + uri.toString());
        }


    }

    public Uri insertProduct(Uri uri, ContentValues values) {

        // Check that the name is not null
        String name = values.getAsString(InvContract.InvEntry.COL_NAME);
        if (name == null) {
            throw new IllegalArgumentException(String.valueOf(R.string.need_name));
        }

        // Check that the gender is valid
        Integer quantity = values.getAsInteger(InvContract.InvEntry.COL_QUANTITY);

        // If the weight is provided, check that it's greater than or equal to 0 kg
        Float price = values.getAsFloat(InvContract.InvEntry.COL_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException(String.valueOf(R.string.need_price));
        }


        // Get database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();


        // Insert the new product with inserted values
        long id = database.insert(InvContract.InvEntry.TABLE_NAME, null, values);
        // If the ID is -1, failed insertion.
        if (id == -1) {
            return null;
        }

        // Notify all listeners changed for the inventory content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID appended at the end
        return ContentUris.withAppendedId(uri, id);
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case INVENTORY:
                rowsDeleted = database.delete(InvContract.InvEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case INVENTORY_ID:
                selection = InvContract.InvEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InvContract.InvEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException(R.string.delete_not_possible + uri.toString());
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int rowsUpdated;

        if (contentValues == null) {
            throw new IllegalArgumentException(String.valueOf(R.string.update_not_possible));
        }

        switch (match) {
            case INVENTORY:
                rowsUpdated = database.update(InvContract.InvEntry.TABLE_NAME,
                        contentValues,
                        selection,
                        selectionArgs);
                break;
            case INVENTORY_ID:
                rowsUpdated = database.update(InvContract.InvEntry.TABLE_NAME,
                        contentValues,
                        InvContract.InvEntry._ID + " = ?",
                        new String[]{String.valueOf(ContentUris.parseId(uri))});
                break;
            default:
                throw new UnsupportedOperationException(R.string.Unknown_URI + uri.toString());
        }

        return rowsUpdated;
    }

    }