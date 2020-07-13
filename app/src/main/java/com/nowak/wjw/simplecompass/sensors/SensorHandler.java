package com.nowak.wjw.simplecompass.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class SensorHandler implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mVectorRotationSensor;
    private MutableLiveData<SensorEvent> mSensorEvent = new MutableLiveData<>();


    public SensorHandler(SensorManager sensorManager) {
        mSensorManager = sensorManager;
        mVectorRotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if (mVectorRotationSensor == null) {
            //todo show message and don't allow to use the app
        }
    }

    public void registerListenerForVectorRotationSensor() {
        mSensorManager.registerListener(this, mVectorRotationSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    public void unregisterListenerForVectorRotationSensor() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        mSensorEvent.setValue(event);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public LiveData<SensorEvent> getSensorEvent() {
        return mSensorEvent;
    }
}
