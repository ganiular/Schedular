package com.example.schedular;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;

public class NotificationService extends Service {
    MediaPlayer mediaPlayer;

    public NotificationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        long eventId = intent.getLongExtra(NotificationReceiver.NOTIFICATION_ID, 0);
        String action = intent.getAction();
        launchNotificationActivity(eventId, action);
//
//        // Stop the foreground service
//        stopForeground(true);
//        stopSelf();
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm_sound);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        new Handler().postDelayed(() -> {
            stopSound();
        }, 60 * 1000);

        return START_STICKY;
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
        intent.putExtra(NotificationReceiver.NOTIFICATION_ID, eventId);
        startActivity(intent);
    }

    private void stopSound() {
        if(mediaPlayer != null && mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}