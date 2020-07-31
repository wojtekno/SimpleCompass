package com.nowak.wjw.simplecompass.domain;

import com.nowak.wjw.simplecompass.data.Compass;

import io.reactivex.rxjava3.core.Observable;

public class GetAzimuthUseCase {

    public Observable<Integer> obsAzimuth;

    public GetAzimuthUseCase(Compass compass) {
        //todo set filter so azimuth doesn't change every single point
        obsAzimuth = compass.integerObservable;
    }

}
