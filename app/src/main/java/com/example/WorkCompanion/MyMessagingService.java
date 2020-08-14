package com.example.WorkCompanion;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;

public class MyMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyMessagingService";
    NotificationHelper notificationHelper = new NotificationHelper(MyApplication.getAppContext());

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        notificationHelper.sendHighPriorityNotification(Objects.requireNonNull(remoteMessage.getNotification()).getTitle(),
                remoteMessage.getNotification().getBody(),MapsActivity.class);
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.d(TAG, "onNewToken: New Token Received");
    }
}
