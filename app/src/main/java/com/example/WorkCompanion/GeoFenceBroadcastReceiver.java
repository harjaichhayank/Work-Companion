package com.example.WorkCompanion;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
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
    GeofencingEvent geofencingEvent;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: geoFence added");

        geofencingEvent = GeofencingEvent.fromIntent(intent);

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
                notificationHelper.sendHighPriorityNotification("GEO_FENCE TRANSITION ENTER "," ",MapsActivity.class);
                break;
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Log.d(TAG, "onReceive: " + "GEO_FENCE TRANSITION DWELL");
                Toast.makeText(context, "GEO_FENCE TRANSITION DWELL", Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification("GEO_FENCE TRANSITION DWELL "," ",MapsActivity.class);
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Log.d(TAG, "onReceive: " + "GEO_FENCE TRANSITION EXIT");
                Toast.makeText(context, "GEO_FENCE TRANSITION EXIT", Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification("GEO_FENCE TRANSITION EXIT "," ",MapsActivity.class);
                break;
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class TaskExecuting extends AsyncTask<Void, Float, Void> {
        Context context = MyApplication.getAppContext();
        PendingResult pendingResult;
        Intent intent;
        GeofencingEvent geofencingEvent;
        Location location;
        float[] results = new float[10];
        String ID;

        public TaskExecuting(PendingResult pendingResult, Intent intent, GeofencingEvent geofencingEvent) {
            this.pendingResult = pendingResult;
            this.intent = intent;
            this.geofencingEvent = geofencingEvent;
        }

        HashMap<String,LatLng> getHashMap = HashMapInstance.getHashMap();
        List<Float> setList = HashMapInstance.getList();
        HashMap<String,Float> setDistanceHashMap = HashMapInstance.getDistanceHashMap();

        double geoFenceLatitude;
        double geoFenceLongitude;

        @Override
        protected Void doInBackground(Void... voids) {

            location = geofencingEvent.getTriggeringLocation();
            Log.d(TAG, "onReceive: user location where it got detected: " + location.getLatitude() + " " + location.getLongitude());

            List<Geofence> geoFenceList = geofencingEvent.getTriggeringGeofences();
            for (Geofence geofence: geoFenceList) {

                ID = geofence.getRequestId();
                Log.d(TAG, "onReceive: RequestId of the geoFence: " + ID);
                geoFenceLatitude = Objects.requireNonNull(getHashMap.get(ID)).latitude;
                geoFenceLongitude = Objects.requireNonNull(getHashMap.get(ID)).longitude;
                Log.d(TAG, "onReceive: LatLng in hashMap: " + geoFenceLatitude + " " + geoFenceLongitude);

                Location.distanceBetween(location.getLatitude(),location.getLongitude(),geoFenceLatitude,geoFenceLongitude,results);
                Log.d(TAG, "doInBackground: Location.distanceBetween: " + results[0]);

                setDistanceHashMap.put(ID,results[0]);
            }

            publishProgress(results[0]);

            if (getHashMap.size() >= 2){
                for (Map.Entry<String, LatLng> set : getHashMap.entrySet()) {
                    Log.d(TAG, "onReceive: Map.Entry<String, LatLng>: " + set.getKey() + " = " + set.getValue());
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Float... values) {
            super.onProgressUpdate(values);
            setList.add(results[0]);
            Log.d(TAG, "onProgressUpdate: " + Arrays.toString(setList.toArray()));
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (setList.size() >= 1){
                float minimum = Collections.min(setList);
                Log.d(TAG, "onPostExecute: MINIMUM In the List: " + minimum);
            }

            if (setDistanceHashMap.size() >= 1){
                for (Map.Entry<String, Float> set : setDistanceHashMap.entrySet()) {
                    Log.d(TAG, "onReceive: Map.Entry<String, Float>: " + set.getKey() + " = " + set.getValue());
                }

                float minimum = Collections.min(setDistanceHashMap.values());
                Log.d(TAG, "onPostExecute: MINIMUM In The HashMap: " + minimum);

                for (Map.Entry<String, Float> entry : setDistanceHashMap.entrySet()) {
                    if (Objects.equals(minimum, entry.getValue())) {
                        Log.d(TAG, "onPostExecute: ID reference of the geoFence of the minimum value: " + entry.getKey());
                        Toast.makeText(context, "onPostExecute: ID reference of the geoFence of the minimum value: " + entry.getKey()
                                + " With Minimum Distance Being: " + minimum, Toast.LENGTH_LONG).show();
                    }
                }
            }
            pendingResult.finish();
        }
    }
}