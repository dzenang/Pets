package com.example.android.pets.data;

import android.provider.BaseColumns;

/**
 * Created by dzenang on 21.3.2017.
 */

public final class PetContract {

    // Private constructor to prevent accidental instantiation
    private PetContract() {}

    // Inner class that defines constants for pets table
    public static abstract class PetEntry implements BaseColumns {

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

    }
}
