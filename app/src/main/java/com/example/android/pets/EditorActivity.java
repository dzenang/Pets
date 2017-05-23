/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.pets.data.PetContract.PetEntry;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /** EditText field to enter the pet's name */
    private EditText mNameEditText;

    /** EditText field to enter the pet's breed */
    private EditText mBreedEditText;

    /** EditText field to enter the pet's weight */
    private EditText mWeightEditText;

    /** EditText field to enter the pet's gender */
    private Spinner mGenderSpinner;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = PetEntry.GENDER_UNKNOWN;
    private static final String TAG = CatalogActivity.class.getSimpleName();
    private static final int LOADER_ID = 0;
    // Uri of item for which Editor Activity is opened
    private Uri mItemUri;
    // Member and listener to know if currently viewed pet is changed
    private boolean mPetHasChanged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mPetHasChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Get uri of item clicked
        mItemUri = getIntent().getData();

        // If uri is null then we are adding new pat, if not we edit existing pet
        if (mItemUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_pet));
            // Invalidate menu options so Delete can be hidden for pet which is not yet in database
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_pet));
            getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        // Setup OnTouchListener on all fields so we know if user touched or modified them
        // This is needed to warn user about unsaved changes if some
        mNameEditText.setOnTouchListener(mTouchListener);
        mBreedEditText.setOnTouchListener(mTouchListener);
        mWeightEditText.setOnTouchListener(mTouchListener);
        mGenderSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);

        // If this is a new pet hide Delete option
        if (mItemUri == null) {
            MenuItem deleteItem = menu.findItem(R.id.action_delete);
            deleteItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Insert pet to database
                savePet();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If pet hasn't changed continue with navigating up
                if (!mPetHasChanged) {
                    // Navigate back to parent activity (CatalogActivity)
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
                // Otherwise if there are unsaved changes setup a dialog to warn user
                // Creates click listener to handle users confirmation changes should be discarded
                DialogInterface.OnClickListener discardClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "discard" so navigate up to parent activity (CatalogActivity)
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };
                // Show dialog that notifies the user about unsaved changes
                showUnsavedChangesDialog(discardClickListener);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        // If pet hasn't changed continue with handling back button
        if (!mPetHasChanged) {
            super.onBackPressed();
            return;
        }

        // If pet is changed setup a dialog to warn the user
        // Create click listener to handle users choice to discard changes
        DialogInterface.OnClickListener discardClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // User clicked "discard" so close current activity
                finish();
            }
        };

        // Show dialog warning about unsaved changes
        showUnsavedChangesDialog(discardClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id) {
            case LOADER_ID:
                return new CursorLoader(this, mItemUri, null, null, null, null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        // Move cursor to 0th position before extracting values
        if (data.moveToFirst()) {
            // Extract data from cursor and set them to activity fields
            String name = data.getString(data.getColumnIndex(PetEntry.COLUMN_PET_NAME));
            mNameEditText.setText(name);
            String breed = data.getString(data.getColumnIndex(PetEntry.COLUMN_PET_BREED));
            mBreedEditText.setText(breed);
            mGender = data.getInt(data.getColumnIndex(PetEntry.COLUMN_PET_GENDER));
            mGenderSpinner.setSelection(mGender);
            int weight = data.getInt(data.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT));
            mWeightEditText.setText(Integer.toString(weight));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mNameEditText.setText("");
        mBreedEditText.setText("");
        mGender = PetEntry.GENDER_UNKNOWN;
        mGenderSpinner.setSelection(mGender);
        mWeightEditText.setText("");
    }

    /**
     * Method to create dialog when user wants to leave EditorActivity without saving
     */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create AlertDialog.Builder and set the message and click listeners for positive and
        // negative buttons on the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // User clicked "keep editing" so dismiss the dialog and continue editing
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });

        // Create and show alert dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */

    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetEntry.GENDER_MALE;
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetEntry.GENDER_FEMALE;
                    } else {
                        mGender = PetEntry.GENDER_UNKNOWN;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = PetEntry.GENDER_UNKNOWN;
            }
        });
    }

    /**
     * Method to insert new pet into database and return rowId of inserted pet of -1 for error
     */
    private void savePet() {
        String nameString = mNameEditText.getText().toString().trim();
        String breedString = mBreedEditText.getText().toString().trim();
        String weightString = mWeightEditText.getText().toString().trim();
        int weightValue = 0;
        if (!TextUtils.isEmpty(weightString)) {
            weightValue = Integer.parseInt(weightString);
        }

        // Check if user is going to save completely empty pet
        if (TextUtils.isEmpty(nameString) && TextUtils.isEmpty(breedString) && weightValue == 0
                && mGender == PetEntry.GENDER_UNKNOWN) {
            finish();
            return;
        }

        // If user wants to leaves name blank
        if (TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, R.string.editor_empty_name, Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, nameString);
        values.put(PetEntry.COLUMN_PET_BREED, breedString);
        values.put(PetEntry.COLUMN_PET_GENDER, mGender);
        values.put(PetEntry.COLUMN_PET_WEIGHT, weightValue);

        // If item uri is null it is "add new pet" mode
        if (mItemUri == null) {
            Uri retUri = getContentResolver().insert(PetEntry.CONTENT_URI, values);
            if (retUri != null) {
                Toast.makeText(this, R.string.pet_saved, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.pet_save_error, Toast.LENGTH_SHORT).show();
            }
        } else { // update existing pet mode
            int rowsUpdated = getContentResolver().update(mItemUri, values, null, null);
            if (rowsUpdated == 1) {
                Toast.makeText(this, R.string.pet_updated, Toast.LENGTH_SHORT).show();
            } else if (rowsUpdated == 0){
                Toast.makeText(this, R.string.pet_update_error, Toast.LENGTH_SHORT).show();
            }
        }
        // Go back to CatalogActivity
        finish();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deletePet();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
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
     * Perform the deletion of the pet in the database.
     */
    private void deletePet() {
        // Only perform delete if it is existing pet
        if(mItemUri != null) {
            int rowsDeleted = getContentResolver().delete(mItemUri, null, null);
            if (rowsDeleted == 1) {
                Toast.makeText(this, R.string.editor_delete_pet_successful, Toast.LENGTH_SHORT).show();
            } else if (rowsDeleted == 0) {
                Toast.makeText(this, R.string.editor_delete_pet_failed, Toast.LENGTH_SHORT).show();
            }
        }
        // Close activity, go back to CatalogActivity
        finish();
    }
}