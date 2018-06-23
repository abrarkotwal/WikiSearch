package com.abrarkotwal.wikisearch.Other;

import android.app.Application;

public class MyApplication extends Application{
    private static MyApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
    }

    public static synchronized MyApplication getInstance() {
        return mInstance;
    }

    public void setConnectivityListener(NetworkBroadcastReceiver.ConnectivityReceiverListener listener) {
        NetworkBroadcastReceiver.connectivityReceiverListener = listener;
    }
}
