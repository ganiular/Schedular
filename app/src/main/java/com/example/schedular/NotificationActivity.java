package com.example.schedular;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.example.schedular.provider.EventProvider;
import com.example.schedular.utils.Converter;

import java.util.Calendar;

public class NotificationActivity extends AppCompatActivity {
    EventProvider eventProvider;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        recreate();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        long eventId = intent.getLongExtra(NotificationReceiver.NOTIFICATION_ID, 0);
        eventProvider = new EventProvider(this);
        EventProvider.EventModel event = eventProvider.getEvent(eventId);

        switch (intent.getAction()) {
            case NotificationReceiver.ACTION_END_SESSION:
                setContentView(R.layout.activity_notification_3);
                findViewById(R.id.exitBtn).setOnClickListener(view -> finish());
                break;
            case NotificationReceiver.ACTION_REMINDER:
                setContentView(R.layout.activity_notification);
                findViewById(R.id.startNowBtn).setOnClickListener(view -> {
                    Intent startSessionIntent = new Intent(NotificationReceiver.ACTION_START_SESSION);
                    startSessionIntent.putExtra(NotificationReceiver.NOTIFICATION_ID, eventId);
                    sendBroadcast(startSessionIntent);
                });
                Button snoozeBtn = findViewById(R.id.snoozeBtn);
                snoozeBtn.setText("Snooze (" + event.snooze + " minutes)");
                snoozeBtn.setOnClickListener(view -> snoozeEvent(event));
                findViewById(R.id.cancelBtn).setOnClickListener(view -> {
                    cancelAlarm(eventId, NotificationReceiver.ACTION_START_SESSION);
                    stopNotificationSound();
                    finish();
                });
                setAlarm(event.id, event.remind, NotificationReceiver.ACTION_START_SESSION);
                break;
            case NotificationReceiver.ACTION_START_SESSION:
                setContentView(R.layout.activity_notification_2);
                findViewById(R.id.exitBtn).setOnClickListener(view -> finish());
                findViewById(R.id.endBtn).setOnClickListener(view -> endEventActivities(event));
                startEventActivities(event);
                break;
        }

        TextView eventName = findViewById(R.id.eventNameTxt);
        TextView eventTime = findViewById(R.id.eventTimeTxt);
        eventName.setText(event.title);
        eventTime.setText(Converter.timeFormatToAM_PM(event.hour, event.minute));
        TextView eventEndTime = findViewById(R.id.eventEndTimeTxt);
        eventEndTime.setText(Converter.timeFormatToAM_PM(event.hour, event.minute + event.duration));
    }

    private void setAlarm(long eventId, int time, String action) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // Create a BroadcastReceiver to handle the notification
        Intent notificationIntent = new Intent(this, NotificationReceiver.class);
        notificationIntent.setAction(action);
        notificationIntent.setData(Uri.parse("custom:" + eventId));
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION_ID, eventId); // Unique ID for the notification (optional)

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int) eventId, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Calculate the alarm time based on the remaining time
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.MINUTE, time);

        // Set the alarm to trigger at the calculated time
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    private void snoozeEvent(EventProvider.EventModel event) {
        eventProvider.snoozeEvent(event);
        cancelAlarm(event.id, NotificationReceiver.ACTION_START_SESSION);
        setAlarm(event.id, (int) event.getEventRemainingTime(), NotificationReceiver.ACTION_REMINDER);
        stopNotificationSound();
        finish();
    }

    private void stopNotificationSound() {
        stopService(new Intent(this, NotificationService.class));
    }

    private void cancelAlarm(long eventId, String action) {
        Intent notificationIntent = new Intent(this, NotificationReceiver.class);
        notificationIntent.setAction(action);
        notificationIntent.setData(Uri.parse("custom:" + eventId));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int) eventId, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    private void startEventActivities(EventProvider.EventModel event) {
        eventProvider.activateActivities(this, event);

        if (event.duration > 0){
            setAlarm(event.id, event.duration, NotificationReceiver.ACTION_END_SESSION);
        }
    }

    private void endEventActivities(EventProvider.EventModel event){
        cancelAlarm(event.id, NotificationReceiver.ACTION_END_SESSION);
        eventProvider.deactivateActivities(this, event);
        finish();
    }
}