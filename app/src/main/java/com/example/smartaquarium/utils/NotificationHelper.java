package com.example.smartaquarium.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import com.example.smartaquarium.R;

public class NotificationHelper {

    private static final String CHANNEL_ID = "AQUARIUM_ALERTS";
    private static final String CHANNEL_NAME = "Aquarium Health Alerts";
    private final Context context;

    public NotificationHelper(Context context) {
        this.context = context;
        initChannel();
    }

    private void initChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifications for sensor limits");

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public void sendAlert(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
        Log.d("NotificationHelper", "Sending notification: " + title);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            Log.d("NotificationHelper", "Sending notification: " + title);
            manager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }
}