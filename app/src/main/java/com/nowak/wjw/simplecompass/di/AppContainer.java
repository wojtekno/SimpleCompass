package com.nowak.wjw.simplecompass.di;

import android.content.Context;
import android.hardware.SensorManager;

import com.google.android.gms.location.LocationServices;
import com.nowak.wjw.simplecompass.data.Compass;
import com.nowak.wjw.simplecompass.domain.GetAzimuthUseCase;
import com.nowak.wjw.simplecompass.domain.GetDestinationBearingUseCase;
import com.nowak.wjw.simplecompass.domain.InitiateLastLocationUseCase;
import com.nowak.wjw.simplecompass.domain.RequestAndStopLocationUpdatesUseCase;
import com.nowak.wjw.simplecompass.domain.ManageSensorListenerUseCase;
import com.nowak.wjw.simplecompass.data.location.LocationApiHandler;
import com.nowak.wjw.simplecompass.data.sensors.SensorHandler;
import com.nowak.wjw.simplecompass.ui.main.MainViewModelFactory;

public class AppContainer {

    public SensorManager mSensorManager;
    private SensorHandler mSensorHandler;
    private LocationApiHandler mLocationApiHandler;

    public AppContainer(Context ctx) {
        mSensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        mSensorHandler = new SensorHandler(mSensorManager);
        mLocationApiHandler = new LocationApiHandler(LocationServices.getFusedLocationProviderClient(ctx));
    }

    private Compass compass() {
        return new Compass(mSensorHandler);
    }

    private GetAzimuthUseCase getAzimuthUseCase() {
        return new GetAzimuthUseCase(compass());
    }

    private ManageSensorListenerUseCase startStopSensorListenerUseCase() {
        return new ManageSensorListenerUseCase(mSensorHandler);
    }

    private InitiateLastLocationUseCase initiateLastLocationUseCase() {
        return new InitiateLastLocationUseCase(mLocationApiHandler);
    }

    private RequestAndStopLocationUpdatesUseCase requestAndStopLocationUpdatesUseCase() {
        return new RequestAndStopLocationUpdatesUseCase(mLocationApiHandler);
    }

    private GetDestinationBearingUseCase getLocationUseCase() {
        return new GetDestinationBearingUseCase(mLocationApiHandler);
    }

    public MainViewModelFactory mainViewModelFactory() {
        return new MainViewModelFactory(getAzimuthUseCase(), startStopSensorListenerUseCase(), initiateLastLocationUseCase(), requestAndStopLocationUpdatesUseCase(), getLocationUseCase());
    }

}
