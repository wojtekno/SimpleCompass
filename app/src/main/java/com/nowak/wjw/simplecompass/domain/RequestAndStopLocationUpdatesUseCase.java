package com.nowak.wjw.simplecompass.domain;

import com.nowak.wjw.simplecompass.data.location.LocationApiHandler;

import timber.log.Timber;

public class RequestAndStopLocationUpdatesUseCase {

    private LocationApiHandler mLocationApiHandler;

    public RequestAndStopLocationUpdatesUseCase(LocationApiHandler locationApiHandler) {
        this.mLocationApiHandler = locationApiHandler;
    }

    public void requestLocationUpdates(boolean passedPermission) {
        Timber.d("startLocationUpdates()");
        mLocationApiHandler.requestLocationUpdates(passedPermission);
    }

    public void stopLocationUpdates() {
        Timber.d("stopLocationUpdates");
        mLocationApiHandler.stopLocationUpdates();
    }
}
