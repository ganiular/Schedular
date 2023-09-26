package com.example.schedular;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "my_database.db";
    private static final int VERSION = 1;
    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS event(" +
                "   id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "   title VARCHAR(50) NOT NULL," +
                "   eventTime TIME NOT NULL," +
                "   duration INT," +
                "   days VERCHAR(8) NOT NULL," +
                "   repeat INT DEFAULT 0," +
                "   reminder INT DEFAULT 0," +
                "   remind INT," +
                "   snooze INT," +
                "   notificationSound VARCHAR(50)," +
                "   muteDevice INT DEFAULT 0," +
                "   ringingVolume INT," +
                "   enableVibration INT DEFAULT 0," +
                "   offMobileData INT DEFAULT 0 )"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public SQLiteDatabase getWriter(){
        return getWritableDatabase();
    }

    public SQLiteDatabase getReader(){
        return getReadableDatabase();
    }
}
