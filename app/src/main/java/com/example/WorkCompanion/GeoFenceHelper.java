package com.example.WorkCompanion;

import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.maps.model.LatLng;

public class GeoFenceHelper extends ContextWrapper {

    PendingIntent pendingIntent;
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
}