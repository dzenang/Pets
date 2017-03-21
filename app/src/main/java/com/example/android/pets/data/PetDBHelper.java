package com.example.android.pets.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.pets.data.PetContract.PetEntry;

/**
 * Created by dzenang on 21.3.2017.
 */

public class PetDBHelper extends SQLiteOpenHelper {

    // Name of database file
    private static final String DATABASE_NAME = "shelter.db";
    // Database version, if you change schema you must increment database version
    private static final int DATABASE_VERSION = 1;
    // String that contains SQL statement to create pets table
    private static final String SQL_CREATE_PETS_TABLE = "CREATE TABLE " + PetEntry.TABLE_NAME + " ("
            + PetEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + ", "
            + PetEntry.COLUMN_PET_NAME + " TEXT NOT NULL" + ", "
            + PetEntry.COLUMN_PET_BREED + " TEXT" + ", "
            + PetEntry.COLUMN_PET_GENDER + " INTEGER NOT NULL" + ", "
            + PetEntry.COLUMN_PET_WEIGHT + " INTEGER NOT NULL DEFAULT 0" + ");";

    public PetDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_PETS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
