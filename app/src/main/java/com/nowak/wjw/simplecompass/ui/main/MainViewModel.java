package com.nowak.wjw.simplecompass.ui.main;

import android.graphics.drawable.TransitionDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.nowak.wjw.simplecompass.R;
import com.nowak.wjw.simplecompass.StateEnum;

import timber.log.Timber;

public class MainViewModel extends ViewModel {
    private MutableLiveData<Integer> mAzimuth = new MutableLiveData<>(0);
    private MutableLiveData<Location> mLocation = new MutableLiveData<>();
    private MutableLiveData<Location> mDestination = new MutableLiveData<>();

    public LiveData<Boolean> foundLastLocation;
    public LiveData<Integer> needleRotation;
    public LiveData<Float> destinationBearing;
    public LiveData<Float> destArrowRotation;
    public LiveData<Float> targetIcTranslationX;
    public LiveData<Float> targetIcTranslationY;
    public LiveData<Boolean> targetIcVisibile;
    public LiveData<Integer> buttonTextResId;// = new MutableLiveData<>(R.string.enter_coordinates_button);
    public LiveData<Boolean> editTextVisible;

    private MutableLiveData<StateEnum> mState = new MutableLiveData<>(StateEnum.COMPASS_ONLY);


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
        targetIcTranslationX = Transformations.map(destArrowRotation, r -> (float) (300 * Math.sin(Math.toRadians(r))));
        targetIcTranslationY = Transformations.map(destArrowRotation, r -> (float) (-300 * Math.cos(Math.toRadians(r))));
        targetIcVisibile = (MutableLiveData<Boolean>) Transformations.map(mState, s -> {
            if (s == StateEnum.SHOW_DESTINATION_AZIMUTH) return true;
            else return false;
        });
        editTextVisible = Transformations.map(mState, s -> {
            if (s == StateEnum.EDIT_DESTINATION_LOCATION) return true;
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
//        Timber.d("locationChanged");
        if (mLocation.getValue() == null || location.getLatitude() != mLocation.getValue().getLatitude() || location.getLongitude() != mLocation.getValue().getLongitude())
            mLocation.setValue(location);
    }

    public LiveData<Integer> getAzimuth() {
        return mAzimuth;
    }

    public void findButtonClicked(String sLat, String sLong) {
        StateEnum lState = mState.getValue();
        if (lState == StateEnum.COMPASS_ONLY) {
            mState.setValue(StateEnum.EDIT_DESTINATION_LOCATION);
            return;
        } else if (lState == StateEnum.EDIT_DESTINATION_LOCATION) {
            mState.setValue(StateEnum.SHOW_DESTINATION_AZIMUTH);
            Double latI = sLat.isEmpty() ? 0.0 : Double.parseDouble(sLat);
            Double longI = sLong.isEmpty() ? 0.0 : Double.parseDouble(sLong);

            Location lDestination;
            lDestination = new Location("manually created special destination");
            lDestination.setLatitude(latI);
            lDestination.setLongitude(longI);
            mDestination.setValue(lDestination);
            mState.setValue(StateEnum.SHOW_DESTINATION_AZIMUTH);
        } else if (lState == StateEnum.SHOW_DESTINATION_AZIMUTH) {
            mState.setValue(StateEnum.COMPASS_ONLY);
        }

    }

}