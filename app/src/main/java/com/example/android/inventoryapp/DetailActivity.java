package com.example.android.inventoryapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.android.inventoryapp.data.InvContract;

import java.io.File;

import static com.example.android.inventoryapp.R.drawable.ic_insert_placeholder;

/**
 * Created by matteo on 05/07/2017.
 */

public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    public static final String TAG = DetailActivity.class.getSimpleName();
    public static final int PICK_PHOTO_REQUEST = 20;
    public static final int EXTERNAL_STORAGE_REQUEST_PERMISSION_CODE = 21;
    //Identifier for the inventory data loader
    private static final int EXISTING_INVENTORY_LOADER = 0;
    //General Product
    public final String[] PRODUCT_COLS = {
            InvContract.InvEntry._ID,
            InvContract.InvEntry.COL_NAME,
            InvContract.InvEntry.COL_QUANTITY,
            InvContract.InvEntry.COL_PRICE,
            InvContract.InvEntry.COL_DESCRIPTION,
            InvContract.InvEntry.COL_ITEMS_SOLD,
            InvContract.InvEntry.COL_SUPPLIER,
            InvContract.InvEntry.COL_PICTURE
    };

    private Uri mCurrentProductUri;

    private ImageView mProductPhoto;
    private EditText mProductName;
    private EditText mProductDescription;
    private EditText mProductInventory;
    private EditText mItemSold;
    private EditText mProductPrice;
    private EditText mSupplier;
    private ImageButton actDelete;
    private ImageButton actOrder;
    private ImageButton actUpdate;
    private TextView lUpdateSave;
    private TextView lOrder;
    private TextView lDelete;
    private ImageButton decreaseQuantity;
    private ImageButton increaseQuantity;
    private ImageButton decreaseQuantitySales;
    private ImageButton increaseQuantitySales;
    private String mCurrentPhotoUri = "no images";
    private String mSudoEmail;
    private String mSudoProduct;
    private int mSudoQuantity = 10;

    private boolean mProductHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);

        mProductPhoto = (ImageView) findViewById(R.id.image_product_photo);
        mProductName = (EditText) findViewById(R.id.inventory_item_name_edittext);
        mProductDescription = (EditText) findViewById(R.id.inventory_item_description_edittext);
        mProductInventory = (EditText) findViewById(R.id.inventory_item_current_quantity_edittext);
        mItemSold = (EditText) findViewById(R.id.current_sales_edittext);
        mProductPrice = (EditText) findViewById(R.id.inventory_item_price_edittext);
        mSupplier = (EditText) findViewById(R.id.suplier_edittext);

        mProductPhoto.setOnTouchListener(mTouchListener);
        mProductName.setOnTouchListener(mTouchListener);
        mProductDescription.setOnTouchListener(mTouchListener);
        mProductInventory.setOnTouchListener(mTouchListener);
        mItemSold.setOnTouchListener(mTouchListener);
        mProductPrice.setOnTouchListener(mTouchListener);
        mSupplier.setOnTouchListener(mTouchListener);

        actDelete = (ImageButton) findViewById(R.id.delete_product_button);
        actOrder = (ImageButton) findViewById(R.id.order_supplier_button);
        actUpdate = (ImageButton) findViewById(R.id.save_product_button);
        lUpdateSave = (TextView) findViewById(R.id.update_save_label);
        lOrder = (TextView) findViewById(R.id.order_label);
        lDelete = (TextView) findViewById(R.id.delete_label);
        decreaseQuantity = (ImageButton) findViewById(R.id.decrease_quantity);
        increaseQuantity = (ImageButton) findViewById(R.id.increase_quantity);
        decreaseQuantitySales = (ImageButton) findViewById(R.id.decrease_quantity_sales);
        increaseQuantitySales = (ImageButton) findViewById(R.id.increase_quantity_sales);

        mProductPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPhotoProductUpdate(view);
            }
        });

        actUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveProduct();
            }
        });

        actDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteConfirmationDialog();
            }
        });

        actOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                orderSupplier();
            }
        });

        //Check where we came from
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        if (mCurrentProductUri == null) {
            //New product
            setTitle(getString(R.string.add_product_title));
            lUpdateSave.setText(R.string.save_product_label);
            actOrder.setVisibility(View.GONE);
            lOrder.setVisibility(View.GONE);
            actDelete.setVisibility(View.GONE);
            lDelete.setVisibility(View.GONE);

        } else {
            //Update product
            setTitle(getString(R.string.edit_product_title));
            lUpdateSave.setText(R.string.update_product_label);

            actOrder.setVisibility(View.VISIBLE);
            lOrder.setVisibility(View.VISIBLE);
            actDelete.setVisibility(View.VISIBLE);
            lDelete.setVisibility(View.VISIBLE);

            getLoaderManager().initLoader(EXISTING_INVENTORY_LOADER, null, this);
        }
        decreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subtractOneToQuantity();
                mProductHasChanged = true;
            }
        });

        increaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sumOneToQuantity();
                mProductHasChanged = true;
            }
        });

        decreaseQuantitySales.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subtractOneToQuantitySales();
                mProductHasChanged = true;
            }
        });

        increaseQuantitySales.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sumOneToQuantitySales();
                mProductHasChanged = true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                //No changes
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(DetailActivity.this);
                    return true;
                }

                //There are some changes
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button
                                NavUtils.navigateUpFromSameTask(DetailActivity.this);
                            }
                        };

                // Notify user that there are unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;

        }

        return super.onOptionsItemSelected(item);

    }


    public void onPhotoProductUpdate(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                invokeGetPhoto();
            } else {
                String[] permisionRequest = {Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions(permisionRequest, EXTERNAL_STORAGE_REQUEST_PERMISSION_CODE);
            }
        } else {
            invokeGetPhoto();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == EXTERNAL_STORAGE_REQUEST_PERMISSION_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            invokeGetPhoto();
        } else {
            Toast.makeText(this, R.string.err_external_storage_permissions, Toast.LENGTH_LONG).show();
        }
    }

    private void subtractOneToQuantity() {
        String previousValueString = mProductInventory.getText().toString();
        int previousValue;
        if (previousValueString.isEmpty()) {
            return;
        } else if (previousValueString.equals("0")) {
            return;
        } else {
            previousValue = Integer.parseInt(previousValueString);
            mProductInventory.setText(String.valueOf(previousValue - 1));
        }
    }

    private void sumOneToQuantity() {
        String previousValueString = mProductInventory.getText().toString();
        int previousValue;
        if (previousValueString.isEmpty()) {
            previousValue = 0;
        } else {
            previousValue = Integer.parseInt(previousValueString);
        }
        mProductInventory.setText(String.valueOf(previousValue + 1));
    }

    private void subtractOneToQuantitySales() {
        String previousValueString = mItemSold.getText().toString();
        int previousValue;
        if (previousValueString.isEmpty()) {
            return;
        } else if (previousValueString.equals("0")) {
            return;
        } else {
            previousValue = Integer.parseInt(previousValueString);
            mItemSold.setText(String.valueOf(previousValue - 1));
        }
    }

    private void sumOneToQuantitySales() {
        String previousValueString = mItemSold.getText().toString();
        int previousValue;
        if (previousValueString.isEmpty()) {
            previousValue = 0;
        } else {
            previousValue = Integer.parseInt(previousValueString);
        }
        mItemSold.setText(String.valueOf(previousValue + 1));
    }

    private void invokeGetPhoto() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String pictureDirectoryPath = pictureDirectory.getPath();

        Uri data = Uri.parse(pictureDirectoryPath);

        photoPickerIntent.setDataAndType(data, "image/*");

        startActivityForResult(photoPickerIntent, PICK_PHOTO_REQUEST);
    }

    @Override
    public void onBackPressed() {
        //No changes
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        //changes
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button
                        finish();
                    }
                };

        // Notify user that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_PHOTO_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                //Uri data is complete
                Uri mProductPhotoUri = data.getData();
                mCurrentPhotoUri = mProductPhotoUri.toString();

                //Import photo
                Glide.with(this).load(mProductPhotoUri)
                        .placeholder(ic_insert_placeholder)
                        .crossFade()
                        .fitCenter()
                        .into(mProductPhoto);
            }
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(this,
                mCurrentProductUri,
                PRODUCT_COLS,
                null,
                null,
                null);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {

            int i_ID = 0;
            int i_COL_NAME = 1;
            int i_COL_QUANTITY = 2;
            int i_COL_PRICE = 3;
            int i_COL_DESCRIPTION = 4;
            int i_COL_ITEMS_SOLD = 5;
            int i_COL_SUPPLIER = 6;
            int i_COL_PICTURE = 7;

            // Extract values from current cursor
            String name = cursor.getString(i_COL_NAME);
            int quantity = cursor.getInt(i_COL_QUANTITY);
            float price = cursor.getFloat(i_COL_PRICE);
            String description = cursor.getString(i_COL_DESCRIPTION);
            int itemSold = cursor.getInt(i_COL_ITEMS_SOLD);
            String supplier = cursor.getString(i_COL_SUPPLIER);
            String photo = cursor.getString(i_COL_PICTURE);
            mCurrentPhotoUri = cursor.getString(i_COL_PICTURE);

            mSudoEmail = "orders@" + supplier + ".com";
            mSudoProduct = name;

            //Updates values on DB
            mProductName.setText(name);
            mProductPrice.setText(String.valueOf(price));
            mProductInventory.setText(String.valueOf(quantity));
            mProductDescription.setText(description);
            mItemSold.setText(String.valueOf(itemSold));
            mSupplier.setText(supplier);
            //Update photo
            Glide.with(this).load(mCurrentPhotoUri)
                    .placeholder(R.mipmap.ic_launcher)
                    .error(ic_insert_placeholder)
                    .crossFade()
                    .fitCenter()
                    .into(mProductPhoto);
        }

    }


    /**
     * Get user input from editor and save/update product into database.
     */
    private void saveProduct() {
        //Read Values from text field
        String nameString = mProductName.getText().toString().trim();
        String descriptionString = mProductDescription.getText().toString().trim();
        String inventoryString = mProductInventory.getText().toString().toString();
        String salesString = mItemSold.getText().toString().trim();
        String priceString = mProductPrice.getText().toString().trim();
        String supplierString = mSupplier.getText().toString().trim();

        //Check if new or updating existing product
        if (TextUtils.isEmpty(nameString) || TextUtils.isEmpty(descriptionString)
                || TextUtils.isEmpty(inventoryString) || TextUtils.isEmpty(salesString)
                || TextUtils.isEmpty(priceString) || TextUtils.isEmpty(supplierString) || (mCurrentPhotoUri == "no images")) {

            Toast.makeText(this, R.string.err_missing_textfields, Toast.LENGTH_SHORT).show();
            // No change
            return;
        }

        //update values
        ContentValues values = new ContentValues();

        values.put(InvContract.InvEntry.COL_NAME, nameString);
        values.put(InvContract.InvEntry.COL_DESCRIPTION, descriptionString);
        values.put(InvContract.InvEntry.COL_QUANTITY, inventoryString);
        values.put(InvContract.InvEntry.COL_ITEMS_SOLD, salesString);
        values.put(InvContract.InvEntry.COL_PRICE, priceString);
        values.put(InvContract.InvEntry.COL_SUPPLIER, supplierString);
        values.put(InvContract.InvEntry.COL_PICTURE, mCurrentPhotoUri);

        if (mCurrentProductUri == null) {

            Uri insertedRow = getContentResolver().insert(InvContract.InvEntry.CONTENT_URI, values);

            if (insertedRow == null) {
                Toast.makeText(this, R.string.err_inserting_product, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.ok_updated, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
        } else {
            // Updating
            int rowUpdated = getContentResolver().update(mCurrentProductUri, values, null, null);

            if (rowUpdated == 0) {
                Toast.makeText(this, R.string.err_inserting_product, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.ok_updated, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);

            }

        }


    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mProductName.setText("");
        mProductPrice.setText("");
        mProductInventory.setText("");
        mProductDescription.setText("");
        mItemSold.setText("");
        mSupplier.setText("");

    }


    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog and set the message
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    /**
     * Prompt the user to confirm that they want to delete this product.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog and set the message
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProducts();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the product in the database.
     */
    private void deleteProducts() {
        // Perform the delete only if this is an existing product.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.

            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    //Order from supplier
    private void orderSupplier() {
        String[] TO = {mSudoEmail};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Order for " + mSudoProduct + " product");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "We need other " + mSudoProduct +
                ". Please send " + mSudoQuantity + " new item.");

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            ex.printStackTrace();
        }
    }
}
