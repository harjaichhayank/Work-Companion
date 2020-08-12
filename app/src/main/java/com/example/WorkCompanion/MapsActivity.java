package com.example.WorkCompanion;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private static final String TAG = "MapsActivity";

    private GoogleMap mMap;
    private int FINE_LOCATION_ACCESS_REQUEST_CODE = 1001;
    private int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 1002;

    private GeofencingClient geofencingClient;
    private GeoFenceHelper geoFenceHelper;

    private Geocoder geocoder;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        geofencingClient = LocationServices.getGeofencingClient(this);
        geoFenceHelper = new GeoFenceHelper(this);

        geocoder = new Geocoder(MapsActivity.this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapsActivity.this);
        LocationRequestUpdates();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            List<Address> addressList = geocoder.getFromLocationName("Sydney",1);
            if (addressList.size() > 0){
                Address address = addressList.get(0);
                LatLng Sydney = new LatLng(address.getLatitude(),address.getLongitude());
                mMap.addMarker(new MarkerOptions()
                        .position(Sydney)
                        .title(address.getLocality()));

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Sydney,15));
            }
        } catch (IOException e) { e.printStackTrace(); }

        enableUserLocation();
        mMap.setOnMapLongClickListener(MapsActivity.this);
    }

    /*-------------------------------------------------------------On Map Click And Handling Map Clicks-----------------------------------------------------*/

    @Override
    public void onMapLongClick(LatLng latLng) {
        if (Build.VERSION.SDK_INT >= 29) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                handleMapLongClick(latLng);
            } else { requestBackgroundLocationPermission(); }
        } else { handleMapLongClick(latLng); }
    }

    private void handleMapLongClick(LatLng latLng) {
        float GEO_FENCE_RADIUS = 300;
        addCircle(latLng, GEO_FENCE_RADIUS);
        addGeocode(latLng);
        addGeoFence(latLng, GEO_FENCE_RADIUS);
    }

    /*------------------------------------------------------------Adding Markers And Circles-------------------------------------------------------------*/

    private void addCircle(LatLng latLng, float radius){
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng)
                .radius(radius)
                .clickable(true)
                .strokeColor(Color.argb(255, 255, 0,0))
                .fillColor(Color.argb(64, 255, 0,0))
                .strokeWidth(4);
        mMap.addCircle(circleOptions);
    }

    private void addGeocode(LatLng latLng) {
        try {
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
            if (addressList.size() > 0){
                Address address = addressList.get(0);
                String streetAddress = address.getAddressLine(0);
                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(streetAddress));
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    /*-----------------------------------------------------------------------Adding GeoFences-----------------------------------------------------------------*/

    private void addGeoFence(LatLng latLng, float geo_fence_radius) {
        String ID = UUID.randomUUID().toString();
        Log.d(TAG, "addGeoFence: String ID generated: " + ID);
        Log.d(TAG, "addGeoFence: LatLng latLng generated: " + latLng);

        Geofence geofence = geoFenceHelper.getGeoFence(ID, latLng, geo_fence_radius, Geofence.GEOFENCE_TRANSITION_ENTER |
                Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);

        GeofencingRequest geofencingRequest = geoFenceHelper.getGeoFencingRequest(geofence);

        PendingIntent pendingIntent = geoFenceHelper.getPendingIntent();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) { return; }
        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) { Log.d(TAG, "onSuccess: GeoFence has been added"); }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        String error = geoFenceHelper.getErrorString(exception);
                        Log.d(TAG, "onFailure: GeoFence not added " + error); }
                });

        HashMap<String, LatLng> setHashMap = HashMapInstance.getHashMap();
        setHashMap.put(ID,latLng);
        Log.d(TAG, "addGeoFence: String ID set To GeoFence: " + ID);
        Log.d(TAG, "addGeoFence: LatLng latLng set To GeoFence: " + latLng);
    }

    /*---------------------------------------------Location Updates through requests and fusedLocationProviderClient + SettingsClient------------------------------------*/

    private void LocationRequestUpdates() {
        locationRequest = LocationRequest.create();
        locationRequest.setFastestInterval(8000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                if (locationResult != null){
                    for (Location location : locationResult.getLocations()){
                        Log.d(TAG, "onLocationResult: " + location.toString());
                        Log.d(TAG, "onLocationResult: " + location.getLatitude());
                        Log.d(TAG, "onLocationResult: " + location.getLongitude());
                    }
                }
            }
        };
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() { fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper()); }

    private void stopLocationUpdates(){ fusedLocationProviderClient.removeLocationUpdates(locationCallback); }

    private void checkSettingsAndStartUpdates(){
        LocationSettingsRequest request = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest).build();

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(request);

        task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.d(TAG, "onSuccess: " + locationSettingsResponse);
                startLocationUpdates();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: " + e.getLocalizedMessage());
                if (e instanceof ResolvableApiException){
                    ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                    try {
                        resolvableApiException.startResolutionForResult(MapsActivity.this,100);
                    } catch (IntentSender.SendIntentException ex) { ex.printStackTrace(); }
                }
            }
        });
    }

    /*-----------------------------------------------------------All The Request Work Is Done Here-------------------------------------------------------*/

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void enableUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            checkSettingsAndStartUpdates();
        } else {
            requestFineLocationPermission();
            requestBackgroundLocationPermission();
        }
    }

    private void requestFineLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("This permission is required due to google safety precautions")
                    .setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
                        }
                    })
                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        } else { ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE); }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void requestBackgroundLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("This permission is required due to google safety precautions")
                    .setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                        }
                    })
                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        } else { ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE); }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) { return; }
                mMap.setMyLocationEnabled(true);
                checkSettingsAndStartUpdates();
                Log.d(TAG, "onRequestPermissionsResult: checkSettingsAndStartUpdates -> granted");
            }
        }

        if (requestCode == BACKGROUND_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED ) { return; }
                mMap.setMyLocationEnabled(true);
                checkSettingsAndStartUpdates();
                Log.d(TAG, "onRequestPermissionsResult: checkSettingsAndStartUpdates -> granted");
            }
        }
    }

    /*--------------------------------------------------Destroying and stopping services like location requests and removing geoFences-----------------------------------------*/

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdates();
        mMap.clear();
        geofencingClient.removeGeofences(geoFenceHelper.getPendingIntent());
        Log.d(TAG, "onStop: geoFencingClient.removeGeoFences");
        Log.d(TAG, "onStop: Map is cleared");
        Log.d(TAG, "onStop: stopLocationUpdates");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        mMap.clear();
        geofencingClient.removeGeofences(geoFenceHelper.getPendingIntent());
        Log.d(TAG, "onDestroy: geoFencingClient.removeGeoFences");
        Log.d(TAG, "onDestroy: Map is cleared");
        Log.d(TAG, "onDestroy: stopLocationUpdates");
    }
}