package com.nowak.wjw.simplecompass.data;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import com.nowak.wjw.simplecompass.data.sensors.SensorHandler;

import io.reactivex.rxjava3.core.Observable;

public class Compass {

    public Observable<Integer> integerObservable;

    public Compass(SensorHandler sensorHandler) {
        integerObservable = sensorHandler.mEventObservable
                .filter(sensorEvent -> sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR)
                .map(sensorEvent -> countAzimuth(sensorEvent));
    }

    private Integer countAzimuth(SensorEvent event) {
        float[] orientation = new float[3];
        float[] rMat = new float[9];
        // calculate th rotation matrix
        SensorManager.getRotationMatrixFromVector(rMat, event.values);
        int lAzimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
        return lAzimuth;
    }
}
