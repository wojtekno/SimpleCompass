package com.nowak.wjw.simplecompass.ui.main;

import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.nowak.wjw.simplecompass.CompassStateEnum;
import com.nowak.wjw.simplecompass.Event;
import com.nowak.wjw.simplecompass.R;
import com.nowak.wjw.simplecompass.domain.GetAzimuthUseCase;
import com.nowak.wjw.simplecompass.domain.StartStopSensorListenerUseCase;

import timber.log.Timber;

public class MainViewModel extends ViewModel {
    private StartStopSensorListenerUseCase mStartStopSensorListenerUseCase;
    private LiveData<Integer> mAzimuth;

    private MutableLiveData<Location> mLocation = new MutableLiveData<>();
    private MutableLiveData<Location> mDestination = new MutableLiveData<>();
    private MutableLiveData<Event<Boolean>> mFoundLastLocation = new MutableLiveData<>(new Event<>(false));
    private LiveData<Float> mDestinationBearing;
    private MutableLiveData<CompassStateEnum> mState = new MutableLiveData<>(CompassStateEnum.COMPASS_ONLY);
    private MutableLiveData<Boolean> mStopLocationUpdates = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> mStartLocationUpdates = new MutableLiveData<>(false);


    public LiveData<Integer> needleRotation;
    public LiveData<Float> destArrowRotation;
    public LiveData<Float> targetIcTranslationX;
    public LiveData<Float> targetIcTranslationY;
    public LiveData<Boolean> targetIcVisible;
    public LiveData<Integer> buttonTextResId;
    public LiveData<Boolean> editTextVisible;
    public LiveData<Boolean> hideKeyBoard;
    public LiveData<Boolean> needScreenOrientation;
    private MutableLiveData<Integer> mScreenOrientation = new MutableLiveData<>(0);

    public MainViewModel(GetAzimuthUseCase getAzimuthUseCase, StartStopSensorListenerUseCase startStopSensorListenerUseCase) {
        Timber.d("MainViewModel::newInstance(GetAzimuthUseCase)");
        mStartStopSensorListenerUseCase = startStopSensorListenerUseCase;

        //azimuth feature
        mAzimuth = getAzimuthUseCase.azimuth;
        needScreenOrientation = Transformations.map(mAzimuth, a -> true);
        needleRotation = Transformations.switchMap(mAzimuth, azimuth ->
                Transformations.map(mScreenOrientation, o -> azimuth + (o * 90))
        );

        //rest
        mDestinationBearing = Transformations.switchMap(mLocation, l -> Transformations.map(mDestination, d -> l.bearingTo(d)));
        destArrowRotation = Transformations.switchMap(mDestinationBearing, b -> Transformations.map(needleRotation, r -> r + b));
        targetIcTranslationX = Transformations.map(destArrowRotation, r -> (float) (300 * Math.sin(Math.toRadians(r))));
        targetIcTranslationY = Transformations.map(destArrowRotation, r -> (float) (-300 * Math.cos(Math.toRadians(r))));
        targetIcVisible = (MutableLiveData<Boolean>) Transformations.map(mState, s -> {
            if (s == CompassStateEnum.SHOW_DESTINATION_AZIMUTH) return true;
            else return false;
        });
        editTextVisible = Transformations.map(mState, s -> {
            if (s == CompassStateEnum.EDIT_DESTINATION_LOCATION) return true;
            else return false;
        });
        buttonTextResId = Transformations.map(mState, s -> {
            switch (s) {
                case COMPASS_ONLY:
                    return R.string.enter_coordinates_button;
                case EDIT_DESTINATION_LOCATION:
                    return R.string.guide_button;
                case SHOW_DESTINATION_AZIMUTH:
                    return R.string.stop_button;
                default:
                    throw new UnsupportedOperationException();
            }
        });

        hideKeyBoard = Transformations.map(mState, s -> {
            if (s != CompassStateEnum.EDIT_DESTINATION_LOCATION) return true;
            else return false;
        });
    }

    public void locationChanged(Location location) {
        Timber.d("locationChanged");
        if (mLocation.getValue() == null || location.getLatitude() != mLocation.getValue().getLatitude() || location.getLongitude() != mLocation.getValue().getLongitude()) {
            mLocation.setValue(location);
        }
        if (mFoundLastLocation.getValue().peekContent() == false) {
            mFoundLastLocation.setValue(new Event<>(true));
        }
    }

    public void findButtonClicked(String sLat, String sLong) {
        CompassStateEnum lState = mState.getValue();
        if (lState == CompassStateEnum.COMPASS_ONLY) {
            mState.setValue(CompassStateEnum.EDIT_DESTINATION_LOCATION);
            return;
        } else if (lState == CompassStateEnum.EDIT_DESTINATION_LOCATION) {
            mState.setValue(CompassStateEnum.SHOW_DESTINATION_AZIMUTH);
            Double latI = sLat.isEmpty() ? 0.0 : Double.parseDouble(sLat);
            Double longI = sLong.isEmpty() ? 0.0 : Double.parseDouble(sLong);

            Location lDestination;
            lDestination = new Location("manually created special destination");
            lDestination.setLatitude(latI);
            lDestination.setLongitude(longI);
            mDestination.setValue(lDestination);
            mStartLocationUpdates.setValue(true);
        } else if (lState == CompassStateEnum.SHOW_DESTINATION_AZIMUTH) {
            mState.setValue(CompassStateEnum.COMPASS_ONLY);
            mStopLocationUpdates.setValue(true);
        }

    }

    public LiveData<CompassStateEnum> getState() {
        return mState;
    }

    public LiveData<Event<Boolean>> getmFoundLastLocation() {
        return mFoundLastLocation;
    }

    public LiveData<Boolean> stopLocationUpdates() {
        return mStopLocationUpdates;
    }

    public LiveData<Boolean> startLocationUpdates() {
        return mStartLocationUpdates;
    }

    public void provideScreenRotation(int screenRotation) {
        if (mScreenOrientation.getValue() != screenRotation) {
            mScreenOrientation.setValue(screenRotation);
        }
    }

    public void onResume() {
        mStartStopSensorListenerUseCase.registerListenerForVectorRotationSensor();
    }


    public void onPause() {
        mStartStopSensorListenerUseCase.unregisterListenerForVectorRotationSensor();
    }

}