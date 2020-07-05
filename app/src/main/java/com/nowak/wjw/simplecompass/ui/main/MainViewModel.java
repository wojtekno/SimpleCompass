package com.nowak.wjw.simplecompass.ui.main;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.nowak.wjw.simplecompass.CompassStateEnum;
import com.nowak.wjw.simplecompass.Event;
import com.nowak.wjw.simplecompass.R;

import timber.log.Timber;

public class MainViewModel extends ViewModel {
    private MutableLiveData<Integer> mAzimuth = new MutableLiveData<>(0);
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


    public MainViewModel() {
        Timber.d("newInstance()");
        needleRotation = Transformations.map(mAzimuth, a -> -a);
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


    public void onSensorChanged(SensorEvent event, int screenOrientation) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            float[] orientation = new float[3];
            float[] rMat = new float[9];
            // calculate th rotation matrix
            SensorManager.getRotationMatrixFromVector(rMat, event.values);
            if (screenOrientation < 0 || 3 < screenOrientation) {
                throw new UnsupportedOperationException();
            }
            int compensationAngle = screenOrientation * 90;
            // get the azimuth value (orientation[0]) in degree - adjusted by the screenOrientation
            int lAzimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360 + compensationAngle) % 360;
            if (lAzimuth != mAzimuth.getValue()) mAzimuth.setValue(lAzimuth);
        }
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

    public LiveData<Integer> getAzimuth() {
        return mAzimuth;
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
}