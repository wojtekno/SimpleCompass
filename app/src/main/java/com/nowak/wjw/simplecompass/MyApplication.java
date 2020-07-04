package com.nowak.wjw.simplecompass;

import android.app.Application;

import com.nowak.wjw.simplecompass.di.AppContainer;
import com.nowak.wjw.simplecompass.log.MyDebugTree;

import timber.log.Timber;

public class MyApplication extends Application {

    public AppContainer appContainer;

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new MyDebugTree());
            Timber.d("MyDebugTree planted");
        }

        appContainer = new AppContainer(getApplicationContext());
    }
}
