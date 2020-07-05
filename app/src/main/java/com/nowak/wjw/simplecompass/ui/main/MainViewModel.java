package com.nowak.wjw.simplecompass.ui.main;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import timber.log.Timber;

public class MainViewModel extends ViewModel {
    private MutableLiveData<Integer> mAzimuth = new MutableLiveData<>(0);
    private MutableLiveData<Location> mLocation = new MutableLiveData<>();
    private MutableLiveData<Location> mDestination = new MutableLiveData<>();

    public LiveData<Boolean> foundLastLocation;
    public LiveData<Integer> needleRotation;
    public LiveData<Float> destinationBearing;
    public LiveData<Float> destArrowRotation;
    //todo remove
    public MutableLiveData<String> debugRotationAngle = new MutableLiveData<>();

    public MainViewModel() {
        Timber.d("newInstance()");
        needleRotation = Transformations.map(mAzimuth, a -> -a);
        destinationBearing = Transformations.switchMap(mLocation, l -> Transformations.map(mDestination, d -> {
            float m = l.bearingTo(d);
//            Timber.d("livebearing in constructor %s", m);
            return m;
        }));
        destArrowRotation = Transformations.switchMap(destinationBearing, b -> {
            return Transformations.map(needleRotation, r -> r + b);
        });
        foundLastLocation = Transformations.map(mLocation, l -> true);
    }

    public void onSensorChanged(SensorEvent event, int screenOrientation) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            float[] orientation = new float[3];
            float[] rMat = new float[9];
            // calculate th rotation matrix
            SensorManager.getRotationMatrixFromVector(rMat, event.values);
            // get the azimuth value (orientation[0]) in degree including dependency of screenOrientation
            int lAzimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360 + getCompensationAngle(screenOrientation)) % 360;
            if (lAzimuth != mAzimuth.getValue()) mAzimuth.setValue(lAzimuth);
        }
    }

    public void locationChanged(Location location) {
//        Timber.d("locationChanged");
        if (mLocation.getValue() == null || location.getLatitude() != mLocation.getValue().getLatitude() || location.getLongitude() != mLocation.getValue().getLongitude())
            mLocation.setValue(location);
    }

    //todo change
    private int getCompensationAngle(int screenOrientation) {
        int angle = 0;
        switch (screenOrientation) {
            case 0:
                debugRotationAngle.setValue("rotation 0");
                break;
            case 1:
                debugRotationAngle.setValue("rotation 90 left");
                angle = 90;
                break;
            case 2:
                debugRotationAngle.setValue("rotation 180 upside down ");
                angle = 180;
                break;
            case 3:
                angle = 270;
                debugRotationAngle.setValue("rotation 270 right");
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return angle;
    }

    public LiveData<Integer> getAzimuth() {
        return mAzimuth;
    }

    public void findBtnClicked(double lat, double lon) {
        Location lDestination;
        lDestination = new Location("manually created special destination");
        lDestination.setLatitude(lat);
        lDestination.setLongitude(lon);
        mDestination.setValue(lDestination);
    }

}