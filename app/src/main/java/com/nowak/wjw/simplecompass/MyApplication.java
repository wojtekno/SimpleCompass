package com.nowak.wjw.simplecompass;

import android.app.Application;
import android.util.Config;

import com.nowak.wjw.simplecompass.log.MyDebugTree;

import timber.log.Timber;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new MyDebugTree());
            Timber.d("MyDebugTree planted");
        }
    }
}
