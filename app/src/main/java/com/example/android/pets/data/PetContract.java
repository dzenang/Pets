package com.example.android.pets.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by dzenang on 21.3.2017.
 */

public final class PetContract {

    // Private constructor to prevent accidental instantiation
    private PetContract() {}

    // Content provider URI constants
    public static final String CONTENT_AUTHORITY = "com.example.android.pets";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_PETS = "pets";

    // Inner class that defines constants for pets table
    public static abstract class PetEntry implements BaseColumns {

        // Content URI to access pets data in the content provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PETS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of pets
         */
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                CONTENT_AUTHORITY + "/" + PATH_PETS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single pet
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" +
                CONTENT_AUTHORITY + "/" + PATH_PETS;

        // String constant for table name
        public static final String TABLE_NAME = "pets";

        // String constants for columns in pets table
        public static final String _ID = BaseColumns._ID; //INTEGER
        public static final String COLUMN_PET_NAME = "name"; //TEXT
        public static final String COLUMN_PET_BREED = "breed"; //TEXT
        public static final String COLUMN_PET_GENDER = "gender"; //INTEGER
        public static final String COLUMN_PET_WEIGHT = "weight"; //INTEGER

        // Possible values for gender column
        public static final int GENDER_UNKNOWN = 0;
        public static final int GENDER_MALE = 1;
        public static final int GENDER_FEMALE = 2;

        /**
         * Checks if provided value is one of valid gender values
         */
        public static boolean isValidGender (int value) {
            if (value == GENDER_FEMALE || value == GENDER_MALE || value == GENDER_UNKNOWN) {
                return true;
            }
            return false;
        }

    }
}
