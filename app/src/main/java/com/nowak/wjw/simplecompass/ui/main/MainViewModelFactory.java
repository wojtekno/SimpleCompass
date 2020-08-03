package com.nowak.wjw.simplecompass.ui.main;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.nowak.wjw.simplecompass.domain.GetAzimuthUseCase;
import com.nowak.wjw.simplecompass.domain.GetDestinationBearingUseCase;
import com.nowak.wjw.simplecompass.domain.InitiateLastLocationUseCase;
import com.nowak.wjw.simplecompass.domain.RequestAndStopLocationUpdatesUseCase;
import com.nowak.wjw.simplecompass.domain.ManageSensorListenerUseCase;

import timber.log.Timber;

public class MainViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private GetAzimuthUseCase mGetAzimuthUseCase;
    private ManageSensorListenerUseCase mManageSensorListenerUseCase;
    private InitiateLastLocationUseCase mInitiateLastLocationUseCase;
    private RequestAndStopLocationUpdatesUseCase mRequestAndStopLocationUpdatesUseCase;
    private GetDestinationBearingUseCase mGetDestinationBearingUseCase;

    public MainViewModelFactory(GetAzimuthUseCase getAzimuthUseCase, ManageSensorListenerUseCase manageSensorListenerUseCase, InitiateLastLocationUseCase initiateLastLocationUseCase, RequestAndStopLocationUpdatesUseCase requestAndStopLocationUpdatesUseCase, GetDestinationBearingUseCase getDestinationBearingUseCase) {
        Timber.d("MainViewModelFactory::newInstance");
        mGetAzimuthUseCase = getAzimuthUseCase;
        mManageSensorListenerUseCase = manageSensorListenerUseCase;
        mInitiateLastLocationUseCase = initiateLastLocationUseCase;
        mRequestAndStopLocationUpdatesUseCase = requestAndStopLocationUpdatesUseCase;
        mGetDestinationBearingUseCase = getDestinationBearingUseCase;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new MainViewModel(mGetAzimuthUseCase, mManageSensorListenerUseCase, mInitiateLastLocationUseCase, mRequestAndStopLocationUpdatesUseCase, mGetDestinationBearingUseCase);
    }
}
