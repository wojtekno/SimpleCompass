package com.nowak.wjw.simplecompass.domain;

import com.nowak.wjw.simplecompass.data.sensors.SensorHandler;

public class ManageSensorListenerUseCase {

    private SensorHandler mSensorHandler;

    public ManageSensorListenerUseCase(SensorHandler sensorHandler) {
        mSensorHandler = sensorHandler;
    }

    public void registerListenerForVectorRotationSensor() {
        mSensorHandler.registerListenerForVectorRotationSensor();
    }

    public void unregisterListenerForVectorRotationSensor() {
        mSensorHandler.unregisterListenerForVectorRotationSensor();
    }
}
