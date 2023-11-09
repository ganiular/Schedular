package com.example.schedular;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

public class NotificationService extends Service {
    static final String CHANNEL_ID = SharedConstant.APP_PACKAGE_NAME + ".NOTIFICATION_CHANNEL";
    static final CharSequence CHANNEL_NAME = "Scheduled Events";
    MediaPlayer mediaPlayer;

    public NotificationService() {
    }

    public static Intent createIntent(Context context, String action, long eventId, CharSequence message){
        Intent intent = new Intent(context, NotificationService.class);
        intent.setAction(action);
        intent.putExtra(NotificationReceiver.EVENT_ID, eventId);
        intent.putExtra(NotificationReceiver.NOTIFICATION_REMINDER_MESSAGE, message);
        return intent;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        long eventId = intent.getLongExtra(NotificationReceiver.EVENT_ID, 0);
        CharSequence message = intent.getCharSequenceExtra(NotificationReceiver.NOTIFICATION_REMINDER_MESSAGE);
        String action = intent.getAction();
        launchNotificationActivity(eventId, action);

        createNotificationChannel();
        Notification notification = createNotification(eventId, message);
        startForeground((int) eventId, notification);

        mediaPlayer = MediaPlayer.create(this, R.raw.alarm_sound);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        new Handler().postDelayed(() -> {
            stopSound();
            stopSelf();
        }, 60 * 1000);

        return START_STICKY;
    }

    private Notification createNotification(long eventId, CharSequence message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("Event Reminder")
                .setContentText(message)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setContentIntent(PendingIntent.getActivity(this, 0, getContentIntent(eventId), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE));
        return builder.build();
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        stopSound();
        super.onDestroy();
    }

    private void launchNotificationActivity(long eventId, String action){
        Intent intent = new Intent(this, NotificationActivity.class);
        intent.setAction(action);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(NotificationReceiver.EVENT_ID, eventId);
        startActivity(intent);
    }

    private Intent getContentIntent(long eventId){
        Intent intent = new Intent(this, NotificationActivity.class);
        intent.setAction(NotificationReceiver.ACTION_REMINDER);
        intent.putExtra(NotificationReceiver.EVENT_ID, eventId);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//        intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT| Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }

    private void stopSound() {
        if(mediaPlayer != null && mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}