package com.example.WorkCompanion;

import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HashMapInstance {

    private static HashMap<String, LatLng> hashMap = new HashMap<>();
    public static HashMap<String, LatLng> getHashMap() { return hashMap; }

    private static List<Float> list = new ArrayList<>();
    public static List<Float> getList() {return list;}

    private static HashMap<String, Float> distanceHashMap = new HashMap<>();
    public static HashMap<String, Float> getDistanceHashMap() { return distanceHashMap; }
}