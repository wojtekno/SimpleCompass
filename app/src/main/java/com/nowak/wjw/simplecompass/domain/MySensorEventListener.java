package com.nowak.wjw.simplecompass.domain;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import timber.log.Timber;

public class MySensorEventListener implements SensorEventListener {

    private float[] orientation = new float[3];
    private float[] rMat = new float[9];
    private MutableLiveData<Integer> mAzimuth = new MutableLiveData<>(0);

    public MySensorEventListener() {
        Timber.d("newInstance()");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
//        Timber.d("onSensorChanged %s", event.values[0]);
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            // calculate th rotation matrix
            SensorManager.getRotationMatrixFromVector(rMat, event.values);
            // get the azimuth value (orientation[0]) in degree
            int lAzimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
            if (lAzimuth != mAzimuth.getValue()) mAzimuth.setValue(lAzimuth);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public LiveData<Integer> getAzimuth() {
        return mAzimuth;
    }
}
