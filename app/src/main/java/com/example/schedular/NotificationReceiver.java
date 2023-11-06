package com.example.schedular;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.legacy.content.WakefulBroadcastReceiver;

import com.example.schedular.provider.EventProvider;

public class NotificationReceiver extends WakefulBroadcastReceiver {
    public final static String NOTIFICATION_ID = "notificationId";
    public static final String ACTION_REMINDER = "com.example.scheduler.REMINDER";
    public static final String ACTION_END_SESSION = "com.example.scheduler.END_SESSION";
    public static final String ACTION_START_SESSION = "com.example.scheduler.START_SESSION";
    public static final String ACTION_STOP_SOUND = "com.example.scheduler.STOP_SOUND";
    private NotificationManagerCompat notificationManager;
    MediaPlayer mediaPlayer;

    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();
        long eventId = intent.getLongExtra(NOTIFICATION_ID, 0);

        switch (intentAction){
            case ACTION_REMINDER:
                Intent intentService = new Intent(context, NotificationService.class);
                intentService.setAction(ACTION_REMINDER);
                intentService.putExtra(NOTIFICATION_ID, eventId);
                ContextCompat.startForegroundService(context, intentService);
                break;
            case ACTION_END_SESSION:
                SharedPreferences mPreferences = context.getSharedPreferences(SharedConstant.APP_PACKAGE_NAME, Context.MODE_PRIVATE);
                long lastEventId = mPreferences.getLong("lastEventId", 0L);
                if(lastEventId == eventId) {
                    launchNotificationActivity(context, eventId, ACTION_END_SESSION);
                }
                break;
            case ACTION_START_SESSION:
                stopSound(context);
                launchNotificationActivity(context, eventId, ACTION_START_SESSION);
                break;
            case ACTION_STOP_SOUND:
                break;
        }
    }

    private void stopSound(Context context) {
        context.stopService(new Intent(context, NotificationService.class));
    }

    private void createNotificationForStart(Context context, long eventId) {
        // Define the notification channel ID and name
        String channelId = "event_reminders";
        CharSequence channelName = "Event Reminders";

        // Create a notification channel (if using Android Oreo or higher)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        int notificationId = (int) eventId; // Retrieve the notification ID (if used)
        Intent mainActivityIntent = new Intent(context, MainActivity.class);
        PendingIntent mainActivityPendingIntent = PendingIntent.getActivities(context, notificationId, new Intent[]{mainActivityIntent}, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_baseline_notifications_active_24)
                .setContentTitle("Event Start")
//                .setContentText("It's time for your event.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true) // Clear notification after click
                .setFullScreenIntent(getPendingIntent(context, eventId, ACTION_START_SESSION), true)
                .setContentIntent(mainActivityPendingIntent);

        notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build()); // Show the notification
    }

    private void createNotificationForCompletion(Context context, long eventId) {
        // Define the notification channel ID and name
        String channelId = "event_reminders";
        CharSequence channelName = "Event Reminders";

        // Create a notification channel (if using Android Oreo or higher)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        int notificationId = (int) eventId; // Retrieve the notification ID (if used)
        Intent mainActivityIntent = new Intent(context, MainActivity.class);
        PendingIntent mainActivityPendingIntent = PendingIntent.getActivities(context, notificationId, new Intent[]{mainActivityIntent}, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_baseline_notifications_active_24)
                .setContentTitle("Event End")
                .setContentText("It's time for your event.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true) // Clear notification after click
                .setFullScreenIntent(getPendingIntent(context, eventId, ACTION_END_SESSION), true)
                .setContentIntent(mainActivityPendingIntent);

        notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build()); // Show the notification
    }

    void createNotification(Context context, long eventId){
        // Define the notification channel ID and name
        String channelId = "event_reminders";
        CharSequence channelName = "Event Reminders";

        // Create a notification channel (if using Android Oreo or higher)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        int notificationId = (int) eventId; // Retrieve the notification ID (if used)
        Intent mainActivityIntent = new Intent(context, MainActivity.class);
        PendingIntent mainActivityPendingIntent = PendingIntent.getActivities(context, notificationId, new Intent[]{mainActivityIntent}, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_baseline_notifications_active_24)
                .setContentTitle("Event Reminder")
                .setContentText("It's time for your event.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true) // Clear notification after click
                .setOngoing(true) // Make the notification persist
                .setFullScreenIntent(getPendingIntent(context, eventId, ACTION_REMINDER), true)
                .setContentIntent(mainActivityPendingIntent);

        notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build()); // Show the notification
    }

    private PendingIntent getPendingIntent(Context context, long eventId, String action) {
        Intent intent = new Intent(context, NotificationActivity.class);
        intent.putExtra(NOTIFICATION_ID, eventId);
        intent.setAction(action);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private void launchNotificationActivity(Context context, long eventId, String action){
        Intent intent = new Intent(context, NotificationActivity.class);
        intent.setAction(action);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(NOTIFICATION_ID, eventId);
        context.startActivity(intent);
    }
}
