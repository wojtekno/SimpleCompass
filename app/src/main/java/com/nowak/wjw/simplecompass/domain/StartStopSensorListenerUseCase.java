package com.nowak.wjw.simplecompass.domain;

import com.nowak.wjw.simplecompass.sensors.SensorHandler;

public class StartStopSensorListenerUseCase {

    private SensorHandler mSensorHandler;

    public StartStopSensorListenerUseCase(SensorHandler sensorHandler) {
        mSensorHandler = sensorHandler;
    }

    public void registerListenerForVectorRotationSensor() {
        mSensorHandler.registerListenerForVectorRotationSensor();
    }

    public void unregisterListenerForVectorRotationSensor() {
        mSensorHandler.unregisterListenerForVectorRotationSensor();
    }
}
