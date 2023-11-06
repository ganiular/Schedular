package com.example.schedular;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "my_database.db";
    private static final int VERSION = 6;
    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS event(" +
                "   id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "   title VARCHAR(50) NOT NULL," +
                "   hour INT NOT NULL," +
                "   minute INT NOT NULL," +
                "   duration INT," +
                "   days VERCHAR(8) NOT NULL," +
                "   repeat INT DEFAULT 0," +
                "   reminder INT DEFAULT 0," +
                "   remind INT," +
                "   snooze INT," +
                "   snoozeCount INT DEFAULT 0," +
                "   notificationSound VARCHAR(50)," +
                "   muteDevice INT DEFAULT 0," +
                "   ringingVolume INT," +
                "   enableVibration INT DEFAULT 0," +
                "   offMobileData INT DEFAULT 0 )"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 6) {
            db.execSQL("DROP TABLE IF EXISTS event");
            onCreate(db);
        }else {
            db.execSQL("ALTER TABLE event ADD snoozeCount INT DEFAULT 0");
        }

        Log.w(this.getClass().getSimpleName(), "onUpgrade(" + oldVersion +", " + newVersion + ")");

    }

    public SQLiteDatabase getWriter(){
        return getWritableDatabase();
    }

    public SQLiteDatabase getReader(){
        return getReadableDatabase();
    }
}
