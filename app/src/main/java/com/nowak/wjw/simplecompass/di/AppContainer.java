package com.nowak.wjw.simplecompass.di;

import android.content.Context;
import android.hardware.SensorManager;

import com.nowak.wjw.simplecompass.Compass;
import com.nowak.wjw.simplecompass.domain.GetAzimuthUseCase;
import com.nowak.wjw.simplecompass.domain.StartStopSensorListenerUseCase;
import com.nowak.wjw.simplecompass.sensors.SensorHandler;
import com.nowak.wjw.simplecompass.ui.main.MainViewModelFactory;

public class AppContainer {

    public SensorManager mSensorManager;
    private SensorHandler mSensorHandler;

    public AppContainer(Context ctx) {
        mSensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        mSensorHandler = new SensorHandler(mSensorManager);
    }

    private Compass compass() {
        return new Compass(mSensorHandler);
    }

    private GetAzimuthUseCase getAzimuthUseCase() {
        return new GetAzimuthUseCase(compass());
    }

    private StartStopSensorListenerUseCase startStopSensorListenerUseCase() {
        return new StartStopSensorListenerUseCase(mSensorHandler);
    }

    public MainViewModelFactory mainViewModelFactory() {
        return new MainViewModelFactory(getAzimuthUseCase(), startStopSensorListenerUseCase());
    }

}
