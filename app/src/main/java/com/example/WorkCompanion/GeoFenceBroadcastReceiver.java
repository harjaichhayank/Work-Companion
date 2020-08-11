package com.example.WorkCompanion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.maps.model.LatLng;
import java.util.List;

public class GeoFenceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "GeoFenceBroadcastRec";
    Location location;
    LatLng latLng;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: geoFence added");

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        GeoFenceHelper geoFenceHelper = new GeoFenceHelper(context);
        NotificationHelper notificationHelper = new NotificationHelper(context);

        if (geofencingEvent.hasError()) {
            Log.d(TAG, "onReceive: Error receiving geoFence event..." + geofencingEvent.getErrorCode());
            return;
        }

        List<Geofence> geoFenceList = geofencingEvent.getTriggeringGeofences();
        for (Geofence geofence: geoFenceList) {
            String ID = geofence.getRequestId();
            Log.d(TAG, "onReceive: RequestId of the geoFence: " + ID);
            latLng = geoFenceHelper.getGeoFenceLatLng(ID);
            Log.d(TAG, "onReceive: LatLng in hashMap: " + latLng);
        }

        location = geofencingEvent.getTriggeringLocation();
        Log.d(TAG, "onReceive: user location where it got detected: " + location.getLatitude() + " " + location.getLongitude());

        int transitionType = geofencingEvent.getGeofenceTransition();

        /*distanceCalculations();*/

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

    /*-----------------------------------------------------------------------Smallest Distance Calculations------------------------------------------------------*/
        /*
    private void distanceCalculations() {
        float[] results = new float[10];
        distanceBetween(location.getLatitude(),location.getLongitude(),latLng.latitude,latLng.longitude,results);
        Toast.makeText(context, "Distance " + results[0], Toast.LENGTH_SHORT).show();
    }
*/
}