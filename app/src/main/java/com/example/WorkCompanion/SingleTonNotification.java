package com.example.WorkCompanion;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import androidx.annotation.RequiresApi;

public class SingleTonNotification {

    public static final String ANDROID_CHANNEL_ID = "com.example.WorkCompanion";
    private static SingleTonNotification ourInstance = new SingleTonNotification();

    private Context appContext;
    public void init(Context context) { if (appContext == null) { this.appContext = context; } }

    public static Context get() { return getInstance().getContext(); }
    public static synchronized SingleTonNotification getInstance() { return ourInstance; }
    private Context getContext() { return appContext; }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void notification(final MapsActivity mapsActivity) {

        NotificationManager notificationManager =(NotificationManager)mapsActivity.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel androidChannel = new NotificationChannel(ANDROID_CHANNEL_ID, "Notification Channel", NotificationManager.IMPORTANCE_DEFAULT);
        androidChannel.enableLights(true);
        androidChannel.enableVibration(true);
        androidChannel.setLightColor(Color.GREEN);
        androidChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        assert notificationManager != null;
        notificationManager.createNotificationChannel(androidChannel);

        Notification notification = new  Notification.Builder(mapsActivity,ANDROID_CHANNEL_ID)
                .setContentTitle("Notification")
                .setContentText("Sample Text")
                .setContentTitle("Sample Subject")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();

        notificationManager.notify(0, notification);
    }
}