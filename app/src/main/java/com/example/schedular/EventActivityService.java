package com.example.schedular;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.example.schedular.provider.EventProvider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class EventActivityService extends Service {
    public static final String ACTION_ACTIVATE_EVENT_ACTIVITIES = ".ACTION_ACTIVATE_EVENT_ACTIVITIES";
    public static final String ACTION_DEACTIVATE_EVENT_ACTIVITIES = ".ACTION_DEACTIVATE_EVENT_ACTIVITIES";
    public EventActivityService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        long eventId = intent.getLongExtra(NotificationReceiver.EVENT_ID, 0);
        switch (intent.getAction()){
            case ACTION_ACTIVATE_EVENT_ACTIVITIES:
                try {
                    activateEventActivities(eventId);
                } catch (SecurityException e){
                    e.printStackTrace();
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                break;
            case ACTION_DEACTIVATE_EVENT_ACTIVITIES:
                try {
                    deactivateActivities(eventId);
                }catch (SecurityException e){
                    e.printStackTrace();
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                break;
        }
        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    @SuppressLint("MissingPermission")
    private void activateEventActivities(long eventId) {
        EventProvider provider = new EventProvider(this);
        EventProvider.EventModel event = provider.getEvent(eventId);

        // Keep current states before change
        SharedPreferences mPreferences = getSharedPreferences(SharedConstant.APP_PACKAGE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.clear();
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        preferencesEditor.putInt("initialRingerMode", audioManager.getRingerMode());
        preferencesEditor.putInt("initialStreamVolume", audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION));
        preferencesEditor.putLong("lastEventId", event.id);
        preferencesEditor.apply();

        // Return snoozeCount back to default
        if(event.snoozeCount > 0){
            provider.clearSnooze(eventId);
        }

        if(event.muteDevice){
            if(event.enableVibration){
                audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            }else {
                audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            }
        } else if(event.enableVibration){
            audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
        } else if(event.ringingVolume > 0){
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, event.ringingVolume, 0);
        }

        if(event.offMobileData){
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    telephonyManager.setDataEnabled(false);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }else{
                try {
                    Method setDataEnabled = telephonyManager.getClass().getDeclaredMethod("setDataEnabled", boolean.class);
                    setDataEnabled.setAccessible(true);
                    setDataEnabled.invoke(telephonyManager, false);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    try {
                        Method setDataEnabled = connectivityManager.getClass().getDeclaredMethod("setMobileDataEnabled", boolean.class);
                        setDataEnabled.setAccessible(true);
                        setDataEnabled.invoke(connectivityManager, false);
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
                        ex.printStackTrace();
                    }

                }
            }
        }
    }

    private void deactivateActivities(long eventId) {
        EventProvider provider = new EventProvider(this);
        EventProvider.EventModel event = provider.getEvent(eventId);

        // Get previous states before change
        SharedPreferences mPreferences = getSharedPreferences(SharedConstant.APP_PACKAGE_NAME, Context.MODE_PRIVATE);
        long lastEventId = mPreferences.getLong("lastEventId", 0);
        if(lastEventId == event.id){
            int ringerMode = mPreferences.getInt("initialRingerMode", 0);
            int ringingVolume = mPreferences.getInt("initialStreamVolume", 0);

            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManager.setRingerMode(ringerMode);
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, ringingVolume, 0);
        }
    }
}