package com.nowak.wjw.simplecompass.ui.main;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.nowak.wjw.simplecompass.domain.GetAzimuthUseCase;
import com.nowak.wjw.simplecompass.domain.StartStopSensorListenerUseCase;

import timber.log.Timber;

public class MainViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private GetAzimuthUseCase mGetAzimuthUseCase;
    private StartStopSensorListenerUseCase mStartStopSensorListenerUseCase;

    public MainViewModelFactory(GetAzimuthUseCase getAzimuthUseCase, StartStopSensorListenerUseCase startStopSensorListenerUseCase) {
        Timber.d("MainViewModelFactory::newInstance");
        mGetAzimuthUseCase = getAzimuthUseCase;
        mStartStopSensorListenerUseCase = startStopSensorListenerUseCase;

    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new MainViewModel(mGetAzimuthUseCase, mStartStopSensorListenerUseCase);
    }
}
