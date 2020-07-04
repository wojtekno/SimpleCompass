package com.nowak.wjw.simplecompass.di;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import timber.log.Timber;

public class AppContainer {

    public SensorManager mSensorManager;
    public Sensor mVectorRotationSensor;

    public AppContainer(Context ctx) {
        mSensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        mVectorRotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        Timber.d("has rotation: %s", mVectorRotationSensor != null);
        if (mVectorRotationSensor == null) {
            //todo show message and don't allow to use the app
        }
    }

    public boolean hasRotationSensor() {
        return mVectorRotationSensor != null;
    }

}
