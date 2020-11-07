package com.example.dustapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationController {

    private Context context;
    private String channel_id = "Miscellaneous";
    private String channel_name = "미세먼지 알림";
    private String channel_description = "미세먼지 관련 정보를 표시합니다";

    public NotificationController(Context context) {
        this.context = context;
        createNotificationChannel(context);
    }

    private void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = channel_name;
            String description = channel_description;
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channel_id, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showNotification(String title, String text) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this.context, channel_id)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        Intent notifyIntent = new Intent(this.context, MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent notifyPendingIntent = PendingIntent.getActivity(
                this.context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(notifyPendingIntent);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this.context);
        notificationManager.notify(1092, builder.build());
    }

    public void showNotification(int type) {
        switch (type) {
            case 0:
                this.showNotification("공기질 좋음", "공기가 맑습니다, 쾌적한 날이에요");
                break;
            case 1:
                this.showNotification("공기질 나쁨", "공기가 탁합니다, 마스크를 착용하세요");
                break;
            case 2:
                this.showNotification("공기질 나쁨", "공기가 탁합니다, 공기청정기를 켜주세요");
                break;
            default:
                this.showNotification("debug", "debug!");
                break;
        }
    }

    public void showErrorNotification() {
        this.showNotification("Error", "error!");
    }
}
