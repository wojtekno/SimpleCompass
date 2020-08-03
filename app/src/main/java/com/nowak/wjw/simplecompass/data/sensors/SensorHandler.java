package com.nowak.wjw.simplecompass.data.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class SensorHandler implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mVectorRotationSensor;
    private BehaviorSubject<SensorEvent> eventProxy = BehaviorSubject.create();
    //todo Q - is it public, private and expose a method, or just a method returning Observable?
    public Observable<SensorEvent> mEventObservable = Observable
            .interval(40, TimeUnit.MILLISECONDS)
            .filter(aLong -> eventProxy.getValue() != null)
            .map(aLong -> eventProxy.getValue())
            .subscribeOn(Schedulers.io());


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
        eventProxy.onNext(event);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
