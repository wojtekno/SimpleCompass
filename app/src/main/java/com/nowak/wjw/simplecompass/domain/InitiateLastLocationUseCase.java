package com.nowak.wjw.simplecompass.domain;

import com.nowak.wjw.simplecompass.data.location.LocationApiHandler;

import timber.log.Timber;

public class InitiateLastLocationUseCase {
    private LocationApiHandler mLocationApiHandler;

    public InitiateLastLocationUseCase(LocationApiHandler locationApiHandler) {
        mLocationApiHandler = locationApiHandler;
    }

    public void initiateLastLocation() {
        Timber.d("initiateLastLocation()");
        mLocationApiHandler.initiateLastLocation();
    }
}
