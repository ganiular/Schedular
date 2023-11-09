package com.example.schedular;

import android.app.Application;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        NotificationReceiver.setAlarms(this);
    }
}
