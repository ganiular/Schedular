package com.example.schedular.provider;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.example.schedular.DatabaseHelper;
import com.example.schedular.NotificationActivity;
import com.example.schedular.SharedConstant;
import com.example.schedular.utils.Converter;
import com.example.schedular.utils.EventComparator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class EventProvider {
    private final DatabaseHelper dbHelper;
    private final String TABLE_NAME = "event";

    public EventProvider(Context context){
        dbHelper = new DatabaseHelper(context);
    }

    @SuppressLint("Range")
    public EventModel getEvent(long rowId){
        EventModel eventModel = null;
        SQLiteDatabase db = dbHelper.getReader();
        Cursor cursor = db.query(TABLE_NAME, null, "id=?", new String[]{""+rowId}, null, null, null, "1");
        if(cursor.moveToNext()){
            eventModel = new EventModel(
                    cursor.getLong(cursor.getColumnIndex("id")),
                    cursor.getString(cursor.getColumnIndex("title")),
                    cursor.getInt(cursor.getColumnIndex("hour")),
                    cursor.getInt(cursor.getColumnIndex("minute")),
                    cursor.getInt(cursor.getColumnIndex("duration")),
                    cursor.getString(cursor.getColumnIndex("days")).split(","),
                    cursor.getInt(cursor.getColumnIndex("repeat")) != 0,
                    cursor.getInt(cursor.getColumnIndex("reminder")) != 0,
                    cursor.getInt(cursor.getColumnIndex("remind")),
                    cursor.getInt(cursor.getColumnIndex("snooze")),
                    cursor.getString(cursor.getColumnIndex("notificationSound")),
                    cursor.getInt(cursor.getColumnIndex("muteDevice")) != 0,
                    cursor.getInt(cursor.getColumnIndex("ringingVolume")),
                    cursor.getInt(cursor.getColumnIndex("enableVibration")) != 0,
                    cursor.getInt(cursor.getColumnIndex("offMobileData")) != 0,
                    cursor.getInt(cursor.getColumnIndex("snoozeCount"))
            );
        }
        cursor.close();
        db.close();
        return eventModel;
    }

    public int deleteEvent(long rowId){
        SQLiteDatabase db = dbHelper.getWriter();
        int result = db.delete(TABLE_NAME, "id=?", new String[]{""+rowId});
        db.close();
        return result;
    }

    public int snoozeEvent(EventModel event){
        ContentValues values = new ContentValues();
        values.put("snoozeCount", "snoozeCount + 1");
        SQLiteDatabase db = dbHelper.getWriter();
        int result = db.update(TABLE_NAME, values,"id=?", new String[]{""+event.id});
        db.close();
        event.snoozeCount += 1;
        return result;
    }

    @SuppressLint("Range")
    public List<EventModel> getAllEvents(){
        List<EventModel> eventList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReader();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
        while(cursor.moveToNext()){
            EventModel eventModel = new EventModel(
                    cursor.getLong(cursor.getColumnIndex("id")),
                    cursor.getString(cursor.getColumnIndex("title")),
                    cursor.getInt(cursor.getColumnIndex("hour")),
                    cursor.getInt(cursor.getColumnIndex("minute")),
                    cursor.getInt(cursor.getColumnIndex("duration")),
                    cursor.getString(cursor.getColumnIndex("days")).split(","),
                    cursor.getInt(cursor.getColumnIndex("repeat")) != 0,
                    cursor.getInt(cursor.getColumnIndex("reminder")) != 0,
                    cursor.getInt(cursor.getColumnIndex("remind")),
                    cursor.getInt(cursor.getColumnIndex("snooze")),
                    cursor.getString(cursor.getColumnIndex("notificationSound")),
                    cursor.getInt(cursor.getColumnIndex("muteDevice")) != 0,
                    cursor.getInt(cursor.getColumnIndex("ringingVolume")),
                    cursor.getInt(cursor.getColumnIndex("enableVibration")) != 0,
                    cursor.getInt(cursor.getColumnIndex("offMobileData")) != 0,
                    cursor.getInt(cursor.getColumnIndex("snoozeCount"))
            );
            eventList.add(eventModel);
        }
        cursor.close();
        db.close();
        Collections.sort(eventList, new EventComparator());
        return eventList;
    }

    public long createEvent(EventModel event){
        return createEvent(event.toContentValue());
    }

    private long createEvent(ContentValues values) {
        SQLiteDatabase db = dbHelper.getWriter();
        long rowId = db.insertOrThrow(TABLE_NAME, null, values);
        db.close();
        return rowId;
    }

//    public void deactivateActivities(Context context, EventModel event) {
//        // Get previous states before change
//        SharedPreferences mPreferences = context.getSharedPreferences(SharedConstant.APP_PACKAGE_NAME, Context.MODE_PRIVATE);
//        long lastEventId = mPreferences.getLong("lastEventId", 0);
//        if(lastEventId == event.id){
//            int ringerMode = mPreferences.getInt("initialRingerMode", 0);
//            int ringingVolume = mPreferences.getInt("initialStreamVolume", 0);
//
//            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//            audioManager.setRingerMode(ringerMode);
//            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, ringingVolume, 0);
//        }
//    }

//    @SuppressLint("MissingPermission")
//    public void activateActivities(Context context, EventModel event){
//        // Keep current states before change
//        SharedPreferences mPreferences = context.getSharedPreferences(SharedConstant.APP_PACKAGE_NAME, Context.MODE_PRIVATE);
//        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
//        preferencesEditor.clear();
//        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//        preferencesEditor.putInt("initialRingerMode", audioManager.getRingerMode());
//        preferencesEditor.putInt("initialStreamVolume", audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION));
//        preferencesEditor.putLong("lastEventId", event.id);
//        preferencesEditor.apply();
//
//        // Return snoozeCount back to default
//        if(event.snoozeCount > 0){
//            ContentValues values = new ContentValues();
//            values.put("snoozeCount", 0);
//            SQLiteDatabase db = dbHelper.getWriter();
//            db.update(TABLE_NAME, values,"id=?", new String[]{""+event.id});
//            db.close();
//        }
//
//        if(event.muteDevice){
//            if(event.enableVibration){
//                audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
//            }else {
//                audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
//            }
//        } else if(event.enableVibration){
//            audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
//        } else if(event.ringingVolume > 0){
//            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, event.ringingVolume, 0);
//        }
//
//        if(event.offMobileData){
//            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                try {
//                    telephonyManager.setDataEnabled(false);
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//            }else{
//                try {
//                    Method setDataEnabled = telephonyManager.getClass().getDeclaredMethod("setDataEnabled", boolean.class);
//                    setDataEnabled.setAccessible(true);
//                    setDataEnabled.invoke(telephonyManager, false);
//                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
//                    e.printStackTrace();
//                    ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//                    try {
//                        Method setDataEnabled = connectivityManager.getClass().getDeclaredMethod("setMobileDataEnabled", boolean.class);
//                        setDataEnabled.setAccessible(true);
//                        setDataEnabled.invoke(connectivityManager, false);
//                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
//                        ex.printStackTrace();
//                    }
//
//                }
//            }
//        }
//    }

    public long createEvent(String title, long time, long duration, String[] days, boolean repeat, boolean reminder, long remind, long snooze, String notificationSound, boolean muteDevice, int ringingVolume, boolean enableVibration, boolean offMobileData){
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

    public long updateEvent(EventModel event, long rowId) {
        ContentValues values = event.toContentValue();
        SQLiteDatabase db = dbHelper.getWriter();
        long result = db.update(TABLE_NAME, values, "id=?", new String[]{"" + rowId});
        db.close();
        return result;
    }

    public void clearSnooze(long eventId) {
        ContentValues values = new ContentValues();
        values.put("snoozeCount", 0);
        SQLiteDatabase db = dbHelper.getWriter();
        db.update(TABLE_NAME, values,"id=?", new String[]{""+eventId});
        db.close();
    }


    public static class EventModel{
        public final long id;
        public String title;
        public int hour;
        public int minute;
        public int duration;
        public String[] days;
        public boolean repeat;
        public boolean reminder;
        public int remind;
        public int snooze;
        public String notificationSound;
        public boolean muteDevice;
        public int ringingVolume;
        public boolean enableVibration;
        public boolean offMobileData;
        String[] daysOfWeek = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        public boolean isCurrent = false;
        public int snoozeCount;


        public EventModel(){
            // set default values
            this.id = 0;
            this.hour = 8;
            this.minute = 0;
            this.duration = 30;
            this.days = new String[]{"null", "null", "null", "null", "null", "null", "null"};
            this.repeat = false;
            this.reminder = false;
            this.remind = 10;
            this.snooze = 10;
            this.notificationSound = null;
            this.muteDevice = false;
            this.ringingVolume = 0;
            this.enableVibration = false;
            this.offMobileData = false;

            // Select today
            Calendar calendar = Calendar.getInstance();
            int todayIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            this.days[todayIndex] = daysOfWeek[todayIndex];

        }

        EventModel(long id, String title, int hour, int minute, int duration, String[] days, boolean repeat, boolean reminder, int remind, int snooze, String notificationSound, boolean muteDevice, int ringingVolume, boolean enableVibration, boolean offMobileData, int snoozeCount) {
            this.id = id;
            this.title = title;
            this.hour = hour;
            this.minute = minute;
            this.duration = duration;
            this.days = days;
            this.repeat = repeat;
            this.reminder = reminder;
            this.remind = remind;
            this.snooze = snooze;
            this.notificationSound = notificationSound;
            this.muteDevice = muteDevice;
            this.ringingVolume = ringingVolume;
            this.enableVibration = enableVibration;
            this.offMobileData = offMobileData;
            this.snoozeCount = snoozeCount;
        }

        public ContentValues toContentValue(){
            ContentValues values = new ContentValues();
            values.put("title", title);
            values.put("hour", hour);
            values.put("minute", minute);
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
            return values;
        }

        public String getTimeString() {
            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            int dayOffset = getDayOffset();
            String nextDay;

            if(dayOffset == 0){
                nextDay = "Today";
            }else if(dayOffset == 1){
                nextDay = "Tomorrow";
            }else{
                nextDay = days[(day + dayOffset) % 7];
            }

            return nextDay + " " + Converter.timeFormatRangeToAM_PM(this.hour, this.minute, this.duration);
        }

        /** Returns 0 for today, 1 for tomorrow, ..., 7 for next week of same day*/
        public int getDayOffset() {
            Calendar calendar = Calendar.getInstance();
            int today = calendar.get(Calendar.DAY_OF_WEEK);
            int dayIndex = today - 1;
            int offset = 0;

            // Find the next non-null day, or loop to the beginning of the week
            while (days[dayIndex].equals("null")) {
                dayIndex = (dayIndex + 1) % 7;
                offset ++;
            }

            // Check if it's today and the time has passed
            if (offset == 0) {
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.add(Calendar.MINUTE, duration);

                // Get the current time
                Calendar currentTime = Calendar.getInstance();

                // If the current time is later than the event time, let dayIndex be 7
                if (currentTime.after(calendar)) {
                    return 7;
                }

                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                if(currentTime.after(calendar)){
                    isCurrent = true;
                }
            }

            return offset;
        }

        /** Returns next day index, 1 for Monday, 2 for Tuesday, ... */
        public int getDayIndex(){
            Calendar calendar = Calendar.getInstance();
            int today = calendar.get(Calendar.DAY_OF_WEEK);
            int dayIndex = today - 1;

            // Find the next non-null day, or loop to the beginning of the week
            while (days[dayIndex].equals("null")) {
                dayIndex = (dayIndex + 1) % 7;
            }
            return dayIndex + 1;
        }

        public Calendar getEndTime(){
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_WEEK, getDayIndex());
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute + duration + getSnoozeSum());
            calendar.set(Calendar.SECOND, 0);
            return calendar;
        }

        /** Get remaining event or snooze time */
        public long getEventRemainingTime() {
            Calendar c = Calendar.getInstance();
            int time;
            int now = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
            if(reminder){
                time = getDayOffset() * 24 * 60 + hour * 60 + minute + getSnoozeSum() - remind;
                if(time <= now){
                    time = getDayOffset() * 24 * 60 + hour * 60 + minute;
                }
            }else {
                time = getDayOffset() * 24 * 60 + hour * 60 + minute;
            }
            long timeDifference = time - now;
            return timeDifference;
        }

        public int getSnoozeSum() {
            return snoozeCount * snooze;
        }
    }
}

