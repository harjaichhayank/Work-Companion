package com.example.WorkCompanion;

import android.app.Application;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class MyApplication extends Application {
    private static List<Context> contextList = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        contextList.add(0, getApplicationContext());
    }

    public static Context getAppContext(){ return contextList.get(0); }
}
