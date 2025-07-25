package com.example.lifegrow.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.lifegrow.R;

public class ToastReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "task_reminder_channel";  // Notification channel ID

    @Override
    public void onReceive(Context context, Intent intent) {
        String taskName = intent.getStringExtra("task_name");
        if (taskName != null) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            // Build URI for the custom sound
            Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.notify);

            // Create notification channel (API 26+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "Task Reminders",
                        NotificationManager.IMPORTANCE_DEFAULT
                );
                channel.setSound(soundUri, new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build());

                notificationManager.createNotificationChannel(channel);
            }

            // Build the notification
            Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("🔔 Task Reminder")
                    .setContentText("Remember to: " + taskName)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setSound(soundUri)  // Custom sound for pre-Oreo devices
                    .setAutoCancel(true)
                    .build();

            // Show the notification
            notificationManager.notify(0, notification);
        }
    }

}
