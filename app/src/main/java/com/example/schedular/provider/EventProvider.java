package com.example.schedular.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.example.schedular.DatabaseHelper;
import com.example.schedular.utils.Converter;

public class EventProvider {
    private final DatabaseHelper dbHelper;
    private final String TABLE_NAME = "events";

    public EventProvider(Context context){
        dbHelper = new DatabaseHelper(context);
    }

    public Bundle getEvent(long rowId){
        Bundle data = null;
        SQLiteDatabase db = dbHelper.getReader();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
        if(cursor.moveToFirst()){
            data = cursor.getExtras();
        }
        cursor.close();
        db.close();
        return data;
    }

    public long createEvent(ContentValues values){
        SQLiteDatabase db = dbHelper.getWriter();
        long rowId = db.insert(TABLE_NAME, null, values);
        db.close();
        return rowId;
    }

    public long createEvent(
            String title,
            long time,
            long duration,
            String[] days,
            boolean repeat,
            boolean reminder,
            long remind,
            long snooze,
            String notificationSound,
            boolean muteDevice,
            int ringingVolume,
            boolean enableVibration,
            boolean offMobileData
    ){
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("time", time);
        values.put("duration", duration);
        values.put("days", Converter.arrayToCommaSeparatedString(days));
        values.put("repeat", repeat);
        values.put("reminder", reminder);
        values.put("remind", remind);
        values.put("snooze", snooze);
        values.put("notificationSound", notificationSound);
        values.put("muteDevice", muteDevice);
        values.put("ringingVolume", ringingVolume);
        values.put("enableVibration", enableVibration);
        values.put("offMobileData", offMobileData);

        return createEvent(values);
    }
}