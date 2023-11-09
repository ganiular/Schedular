package com.example.schedular;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.schedular.provider.EventProvider;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.List;

public class NotificationReceiver extends BroadcastReceiver {
    public final static String EVENT_ID = "eventId";
    public static final String ACTION_REMINDER = "com.example.scheduler.REMINDER";
    public static final String ACTION_END_SESSION = "com.example.scheduler.END_SESSION";
    public static final String ACTION_START_SESSION = "com.example.scheduler.START_SESSION";
    public static final String ACTION_STOP_SOUND = "com.example.scheduler.STOP_SOUND";
    public static final String NOTIFICATION_REMINDER_MESSAGE = "message";
    public static final String NOTIFICATION_EVENT_START_TIME = "eventStartTime";
    public static final String NOTIFICATION_EVENT_END_TIME = "eventEndTime";
    public static final String IS_VALID_EXTRA_DATA = ".OK";
    private static final int NOTIFICATION_ID = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();
        switch (intentAction) {
            case Intent.ACTION_BOOT_COMPLETED:
            case Intent.ACTION_MY_PACKAGE_REPLACED:
                Toast.makeText(context, "Boot completed", Toast.LENGTH_SHORT).show();
                setAlarms(context);
                break;
            default:
                assert intent.getBooleanExtra(IS_VALID_EXTRA_DATA, false);

                long eventId = intent.getLongExtra(EVENT_ID, 0);
                long eventStartTime = intent.getLongExtra(NOTIFICATION_EVENT_START_TIME, 0);
                long eventEndTime = intent.getLongExtra(NOTIFICATION_EVENT_END_TIME, 0);
                CharSequence message = intent.getCharSequenceExtra(NOTIFICATION_REMINDER_MESSAGE);

                switch (intentAction) {
                    case ACTION_REMINDER:
                        Intent startSessionIntent = getIntent(context, eventId, ACTION_START_SESSION, 0, eventEndTime, message);
                        setAlarm(context, startSessionIntent, eventStartTime);

                        // Start service for sound and notification
                        Intent intentService = NotificationService.createIntent(context,ACTION_REMINDER, eventId, message);
                        ContextCompat.startForegroundService(context, intentService);
                        break;
                    case ACTION_START_SESSION:
                        stopSound(context);
                        Intent intentService2 = new Intent(context, EventActivityService.class);
                        makeNotification(context, eventId, message);
                        intentService2.setAction(EventActivityService.ACTION_ACTIVATE_EVENT_ACTIVITIES);
                        intentService2.putExtra(NotificationReceiver.EVENT_ID, eventId);
                        context.startService(intentService2);

                        launchNotificationActivity(context, eventId, ACTION_START_SESSION);
                        Intent endSessionIntent = getIntent(context, eventId, ACTION_END_SESSION, 0, 0, message);
                        setAlarm(context, endSessionIntent, eventEndTime);
                        break;
                    case ACTION_END_SESSION:
                        SharedPreferences mPreferences = context.getSharedPreferences(SharedConstant.APP_PACKAGE_NAME, Context.MODE_PRIVATE);
                        long lastEventId = mPreferences.getLong("lastEventId", 0L);
                        if (lastEventId == eventId) {
                            Intent intentService3 = new Intent(context, EventActivityService.class);
                            intentService3.setAction(EventActivityService.ACTION_DEACTIVATE_EVENT_ACTIVITIES);
                            intentService3.putExtra(NotificationReceiver.EVENT_ID, eventId);
                            context.startService(intentService3);
                            removeNotification(context);

                            launchNotificationActivity(context, eventId, ACTION_END_SESSION);
                        }
                        break;
                    case ACTION_STOP_SOUND:
                        stopSound(context);
                        break;
                }
                break;
        }
    }

    private void removeNotification(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(NOTIFICATION_ID);
    }

    private void makeNotification(Context context, long eventId, CharSequence message) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // create notification channel
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NotificationService.CHANNEL_ID, NotificationService.CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }

        Intent contentIntent = new Intent(context, NotificationActivity.class);
        contentIntent.setAction(NotificationReceiver.ACTION_START_SESSION);
        contentIntent.putExtra(NotificationReceiver.EVENT_ID, eventId);
        contentIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationService.CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("Event Ongoing")
                .setContentText(message)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setOngoing(true)
                .setAutoCancel(false)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(PendingIntent.getActivity(context, 0, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE));
        Notification notification = builder.build();
        manager.notify(NOTIFICATION_ID, notification);
    }

    private void stopSound(Context context) {
        context.stopService(new Intent(context, NotificationService.class));
    }

    private void launchNotificationActivity(Context context, long eventId, String action){
        Intent intent = new Intent(context, NotificationActivity.class);
        intent.setAction(action);
         intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(EVENT_ID, eventId);
        context.startActivity(intent);
    }

    public static Intent getIntent(Context context, long eventId, String action, long startTime, long endTime, @NotNull CharSequence eventTitle){
        assert !action.equals(NotificationReceiver.ACTION_END_SESSION) || startTime == 0 && endTime == 0;
        assert !action.equals(NotificationReceiver.ACTION_START_SESSION) || startTime == 0 && endTime != 0;
        assert !action.equals(NotificationReceiver.ACTION_REMINDER) || startTime != 0 && endTime != 0;

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction(action);
        intent.setData(Uri.parse("custom:" + eventId));
        intent.putExtra(NotificationReceiver.IS_VALID_EXTRA_DATA, true);
        intent.putExtra(NotificationReceiver.EVENT_ID, eventId);
        intent.putExtra(NotificationReceiver.NOTIFICATION_EVENT_START_TIME, startTime);
        intent.putExtra(NotificationReceiver.NOTIFICATION_EVENT_END_TIME, endTime);
        intent.putExtra(NotificationReceiver.NOTIFICATION_REMINDER_MESSAGE, "Incoming event: "+ eventTitle);
        return intent;
    };

    static void setAlarms(Context context) {
        EventProvider provider = new EventProvider(context);
        List<EventProvider.EventModel> events = provider.getAllEvents();
        for(EventProvider.EventModel e: events){
            setAlarm(context, e);
        }
    }

    public static void setAlarm(Context context, EventProvider.EventModel event) {
        // eventCalendar to the end time.
        Calendar eventCalendar = event.getEndTime();

        Calendar now = Calendar.getInstance();
        // If end time is still in future
        if (eventCalendar.after(now)){
            long eventEndTime = eventCalendar.getTimeInMillis();
            // remove duration time to remain event start time
            eventCalendar.add(Calendar.MINUTE, -event.duration);
            // If reminder and there is still time for reminder
            if(event.reminder && eventCalendar.after(now)) {
                long eventStartTime = eventCalendar.getTimeInMillis();
                Intent intent = NotificationReceiver.getIntent(context, event.id, NotificationReceiver.ACTION_REMINDER, eventStartTime, eventEndTime, event.title);

                eventCalendar.add(Calendar.MINUTE, -(event.remind));
                // If reminder is still in future
                if (eventCalendar.after(now)) {
                    setAlarm(context, intent, eventCalendar.getTimeInMillis());
                }else { // Remind now
                    context.getApplicationContext().sendBroadcast(intent);
                }
            }
            // If event is still in future
            else if(eventCalendar.after(now)){
                Intent intent = NotificationReceiver.getIntent(context, event.id, NotificationReceiver.ACTION_START_SESSION, 0, eventEndTime, event.title);
                setAlarm(context, intent, eventCalendar.getTimeInMillis());
            }else { // start event session now
                Intent intent = NotificationReceiver.getIntent(context, event.id, NotificationReceiver.ACTION_START_SESSION, 0, eventEndTime, event.title);
                context.getApplicationContext().sendBroadcast(intent);
            }
        }
    }

    public static void setAlarm(Context context, Intent intent, long eventTimeInMillis) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) intent.getLongExtra(EVENT_ID, 0), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, eventTimeInMillis, pendingIntent);
        }else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, eventTimeInMillis, pendingIntent);
        }
    }

    public static void cancelAlarm(Context context, long eventId, String action) {
        Intent notificationIntent = new Intent(context, NotificationReceiver.class);
        notificationIntent.setAction(action);
        notificationIntent.setData(Uri.parse("custom:" + eventId));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) eventId, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_MUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}
