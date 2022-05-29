package com.example.alarmapp;

import static com.example.alarmapp.ReaderContract.Entry.COLUMN_NAME_HOUR;
import static com.example.alarmapp.ReaderContract.Entry.COLUMN_NAME_MINUTE;
import static com.example.alarmapp.ReaderContract.Entry.ID;
import static com.example.alarmapp.ReaderContract.Entry.TABLE_NAME;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDBHelper extends SQLiteOpenHelper {
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    ID + " INTEGER PRIMARY KEY," +
                    COLUMN_NAME_HOUR + " INTEGER," +
                    COLUMN_NAME_MINUTE + " INTEGER)";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    public MyDBHelper(Context context) {
        super(context, "alarmDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
