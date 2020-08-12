package com.example.WorkCompanion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.maps.model.LatLng;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GeoFenceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "GeoFenceBroadcastRec";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: geoFence added");

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        NotificationHelper notificationHelper = new NotificationHelper(context);

        if (geofencingEvent.hasError()) {
            Log.d(TAG, "onReceive: Error receiving geoFence event..." + geofencingEvent.getErrorCode());
            return;
        }

        Log.d(TAG, "onReceive: PendingResult and New Task: created");
        PendingResult pendingResult = goAsync();
        new TaskExecuting(pendingResult,intent,geofencingEvent).execute();

        Log.d(TAG, "onReceive: transitionTypes created");
        int transitionType = geofencingEvent.getGeofenceTransition();

        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                Log.d(TAG, "onReceive: " + "GEO_FENCE TRANSITION ENTER");
                Toast.makeText(context, "GEO_FENCE TRANSITION ENTER", Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification("Notification Received"," ",MapsActivity.class);
                break;
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Log.d(TAG, "onReceive: " + "GEO_FENCE TRANSITION DWELL");
                Toast.makeText(context, "GEO_FENCE TRANSITION DWELL", Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification("Notification Received"," ",MapsActivity.class);
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Log.d(TAG, "onReceive: " + "GEO_FENCE TRANSITION EXIT");
                Toast.makeText(context, "GEO_FENCE TRANSITION EXIT", Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification("Notification Received"," ",MapsActivity.class);
                break;
        }
    }

    //doInBackGround,onProgressUpdate,onPostExecute
    //Params, Progress, Result
    private static class TaskExecuting extends AsyncTask<Void, float[], Void> {
        PendingResult pendingResult;
        Intent intent;
        GeofencingEvent geofencingEvent;
        Location location;
        float[] results = new float[10];

        HashMap<String,LatLng> getHashMap = HashMapInstance.getHashMap();
        List<Float> getList = HashMapInstance.getList();

        double geoFenceLatitude;
        double geoFenceLongitude;

        public TaskExecuting(PendingResult pendingResult, Intent intent, GeofencingEvent geofencingEvent) {
            this.pendingResult = pendingResult;
            this.intent = intent;
            this.geofencingEvent = geofencingEvent;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            location = geofencingEvent.getTriggeringLocation();
            Log.d(TAG, "onReceive: user location where it got detected: " + location.getLatitude() + " " + location.getLongitude());

            List<Geofence> geoFenceList = geofencingEvent.getTriggeringGeofences();
            for (Geofence geofence: geoFenceList) {

                String ID = geofence.getRequestId();
                Log.d(TAG, "onReceive: RequestId of the geoFence: " + ID);
                geoFenceLatitude = Objects.requireNonNull(getHashMap.get(ID)).latitude;
                geoFenceLongitude = Objects.requireNonNull(getHashMap.get(ID)).longitude;
                Log.d(TAG, "onReceive: LatLng in hashMap: " + geoFenceLatitude + " " + geoFenceLongitude);

                Location.distanceBetween(location.getLatitude(),location.getLongitude(),geoFenceLatitude,geoFenceLongitude,results);
                Log.d(TAG, "doInBackground: Location.distanceBetween: " + results[0]);
            }

            publishProgress(results);

            if (getHashMap.size() >= 2){
                for (Map.Entry<String, LatLng> set : getHashMap.entrySet()) {
                    Log.d(TAG, "onReceive: Map.Entry<String, LatLng>: " + set.getKey() + " = " + set.getValue());
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(float[]... values) {
            super.onProgressUpdate(values);
            getList.add(results[0]);
            Log.d(TAG, "onProgressUpdate: " + Arrays.toString(getList.toArray()));
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (getList.size() >= 1){
                float minimum = Collections.min(getList);
                Log.d(TAG, "onPostExecute: MINIMUM: " + minimum);
            }
            pendingResult.finish();
        }
    }

    /*----------------------------------------------------------------------Additional Information-----------------------------------------------------*/
                                                                    /*int minList = Collections.min(list);*/
                                            /*Value value = Collections.min(map.entrySet(), Map.Entry.comparingByValue()).getValue()*/
}