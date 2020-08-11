package com.example.WorkCompanion;

import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

public class GeoFenceHelper extends ContextWrapper {

    private static final String TAG = "GeoFenceHelper";

    PendingIntent pendingIntent;
    HashMap<String, LatLng> hashMap = new HashMap<>();

    private static final int PENDING_INTENT_REQUEST_CODE = 234;

    public GeoFenceHelper(Context base) { super(base); }

    public Geofence getGeoFence(String ID, LatLng latLng, float radius, int TransitionTypes){
        return new Geofence.Builder()
                .setCircularRegion(latLng.latitude,latLng.longitude,radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setLoiteringDelay(2000)
                .setRequestId(ID)
                .setTransitionTypes(TransitionTypes)
                .build();
    }

    public GeofencingRequest getGeoFencingRequest(Geofence geofence){
        return new GeofencingRequest.Builder()
                .addGeofence(geofence)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .build();
    }

    public PendingIntent getPendingIntent(){
        if (pendingIntent != null){ return pendingIntent; }
        Intent intent = new Intent(GeoFenceHelper.this,GeoFenceBroadcastReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(GeoFenceHelper.this,PENDING_INTENT_REQUEST_CODE,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    public String getErrorString(Exception e) {
        if (e instanceof ApiException) {
            ApiException apiException = (ApiException) e;
            switch (apiException.getStatusCode()) {
                case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                    return "GEO_FENCE NOT AVAILABLE";
                case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                    return "GEO_FENCE TOO MANY GEO_FENCES";
                case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                    return "GEO_FENCE TOO MANY PENDING INTENTS";
            }
        }
        return e.getLocalizedMessage();
    }

    public LatLng getGeoFenceLatLng(String ID) {
        Log.d(TAG, "getGeoFenceLatLng: the Id passed is: " + ID);

        if (this.hashMap.isEmpty()){
            Log.d(TAG, "getGeoFenceLatLng: HashMap is Empty");
            Toast.makeText(this, "HashMap is Empty", Toast.LENGTH_SHORT).show();
        }

        LatLng latLng = hashMap.get(ID);
        Log.d(TAG, "getHashMap: the LatLng in the hashMap is: " + latLng);
        return latLng;
    }

    public void setHashMap(String ID, LatLng latLng) {
        this.hashMap.put(ID,latLng);
        Log.d(TAG, "setHashMap: ID of the geoFence: " + ID);
        Log.d(TAG, "setHashMap: latLng of the geoFence: " + latLng);
    }

    public void clearHashMap(){
        hashMap.clear();
        Log.d(TAG, "clearHashMap: HashMap is clear");
    }
}