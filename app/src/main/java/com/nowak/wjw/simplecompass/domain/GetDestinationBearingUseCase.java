package com.nowak.wjw.simplecompass.domain;

import com.nowak.wjw.simplecompass.data.LocationCoordinates;
import com.nowak.wjw.simplecompass.data.location.LocationApiHandler;

import io.reactivex.rxjava3.core.Observable;

public class GetDestinationBearingUseCase {

    private LocationApiHandler mLocationApiHandler;

    public GetDestinationBearingUseCase(LocationApiHandler locationApiHandler) {
        mLocationApiHandler = locationApiHandler;
    }

    public Observable<Float> getBearingObservable(LocationCoordinates destCoordinates) {
        return mLocationApiHandler.getBearingTo(destCoordinates);
    }

}
