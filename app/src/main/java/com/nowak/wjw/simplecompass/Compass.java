package com.nowak.wjw.simplecompass;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.nowak.wjw.simplecompass.sensors.SensorHandler;

public class Compass {

    private LiveData<Integer> mAzimuth;

    public Compass(SensorHandler sensorHandler) {
        mAzimuth = Transformations.map(sensorHandler.getSensorEvent(), this::countAzimuth);
    }

    public LiveData<Integer> getAzimuth() {
        return mAzimuth;
    }

    private Integer countAzimuth(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            float[] orientation = new float[3];
            float[] rMat = new float[9];
            // calculate th rotation matrix
            SensorManager.getRotationMatrixFromVector(rMat, event.values);
            int lAzimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
            return lAzimuth;
        }
        return mAzimuth.getValue();
    }
}
