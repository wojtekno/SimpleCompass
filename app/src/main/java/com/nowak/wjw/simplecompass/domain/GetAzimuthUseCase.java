package com.nowak.wjw.simplecompass.domain;

import androidx.lifecycle.LiveData;

import com.nowak.wjw.simplecompass.data.Compass;

public class GetAzimuthUseCase {

    public LiveData<Integer> azimuth;

    public GetAzimuthUseCase(Compass compass) {
        //todo set filter so azimuth doesn't change every single point
        azimuth = compass.getAzimuth();
    }

}
