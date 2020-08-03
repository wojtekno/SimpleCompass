package com.nowak.wjw.simplecompass.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.LiveDataReactiveStreams;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.nowak.wjw.simplecompass.Event;
import com.nowak.wjw.simplecompass.R;
import com.nowak.wjw.simplecompass.data.LocationCoordinates;
import com.nowak.wjw.simplecompass.domain.GetAzimuthUseCase;
import com.nowak.wjw.simplecompass.domain.GetDestinationBearingUseCase;
import com.nowak.wjw.simplecompass.domain.InitiateLastLocationUseCase;
import com.nowak.wjw.simplecompass.domain.ManageSensorListenerUseCase;
import com.nowak.wjw.simplecompass.domain.RequestAndStopLocationUpdatesUseCase;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import timber.log.Timber;

public class MainViewModel extends ViewModel {
    private ManageSensorListenerUseCase mManageSensorListenerUseCase;
    private InitiateLastLocationUseCase mInitiateLastLocationUseCase;
    private RequestAndStopLocationUpdatesUseCase mRequestAndStopLocationUpdatesUseCase;
//    private GetDestinationBearingUseCase mGetDestinationBearingUseCase;
    private MutableLiveData<CompassStateEnum> mState = new MutableLiveData<>(CompassStateEnum.COMPASS_ONLY);
    private MutableLiveData<Integer> mScreenOrientation = new MutableLiveData<>(0);
    private LiveData<Integer> mAzimuth;
    private MutableLiveData<LocationCoordinates> mDestinationCoordinates = new MutableLiveData<>();
    private LiveData<Float> mDestinationBearing;

    private boolean hasLocationRequestsPermission = false;
    private boolean isRequestingLocationUpdates = false;

    public LiveData<Boolean> needScreenOrientation;
    public LiveData<Integer> needleRotation;
    public LiveData<Float> destArrowRotation;
    public LiveData<Float> targetIcTranslationX;
    public LiveData<Float> targetIcTranslationY;
    public LiveData<Boolean> targetIcVisible;
    public LiveData<Integer> buttonTextResId;
    public LiveData<Boolean> editTextVisible;
    public LiveData<Boolean> hideKeyBoard;
    public LiveData<Boolean> showLocationError;
    private MutableLiveData<Boolean> mShouldProvideRationale = new MutableLiveData<>(false);
    public LiveData<Event<Boolean>> shouldProvideRationale = Transformations.map(mShouldProvideRationale, should -> new Event(should));
    private MutableLiveData<Boolean> mShowPermissionDenied = new MutableLiveData<>(false);
    public LiveData<Event<Boolean>> showPermissionDenied = Transformations.map(mShowPermissionDenied, show -> new Event<>(show));
    private MutableLiveData<Boolean> mRequestLocationPermission = new MutableLiveData<>(false);
    public LiveData<Event<Boolean>> requestLocationPermission = Transformations.map(mRequestLocationPermission, request -> new Event<>(request));


    public MainViewModel(GetAzimuthUseCase getAzimuthUseCase, ManageSensorListenerUseCase manageSensorListenerUseCase, InitiateLastLocationUseCase initiateLastLocationUseCase, RequestAndStopLocationUpdatesUseCase requestAndStopLocationUpdatesUseCase, GetDestinationBearingUseCase getDestinationBearingUseCase) {
        Timber.d("MainViewModel::newInstance(GetAzimuthUseCase)");

        //azimuth feature
        mManageSensorListenerUseCase = manageSensorListenerUseCase;
//        mAzimuth = getAzimuthUseCase.azimuth;
        mAzimuth = LiveDataReactiveStreams.fromPublisher(getAzimuthUseCase.obsAzimuth
                .toFlowable(BackpressureStrategy.BUFFER)
                .filter(integer -> {
                    if (mAzimuth.getValue() == null) return true;
                    int cA = mAzimuth.getValue();
                    if (Math.abs(cA - integer) > 0) {
                        return true;
                    }
                    return false;
                })
        );
        needScreenOrientation = Transformations.map(mAzimuth, a -> true);
//        todo fix needle rotation - angle miscalculated
        needleRotation = Transformations.switchMap(mAzimuth, azimuth -> {
                    Timber.d("needle rotation and azimuth: %s ", azimuth);
                    return Transformations.map(mScreenOrientation, o -> azimuth + (o * 90));
                }
        );

        //Location feature
        mInitiateLastLocationUseCase = initiateLastLocationUseCase;
        mRequestAndStopLocationUpdatesUseCase = requestAndStopLocationUpdatesUseCase;

//        mGetDestinationBearingUseCase = getDestinationBearingUseCase;
//        mDestinationBearing = Transformations.switchMap(mDestinationCoordinates, c -> {
//            if (c == null) return null;
//            Timber.d("Transformations.switchMap(mDestinationCoordinates");
//            return getDestinationBearingUseCase.getMyDestinationBearing(c);
//        });

        mDestinationBearing = Transformations.switchMap(mDestinationCoordinates, dest -> {
            if (dest == null) return null;
            return LiveDataReactiveStreams.fromPublisher(getDestinationBearingUseCase.getBearingObservable(dest).toFlowable(BackpressureStrategy.BUFFER).filter(aFloat -> {
                Timber.d("subject bearing %s ", aFloat);
                return true;
            }));
        });

        setUpViewsStates();
    }

    private void setUpViewsStates() {
        // todo Q? how to late init below fields
        //views' states
        destArrowRotation = Transformations.switchMap(mDestinationBearing, b -> {
            Timber.d("destArrowRotation");
            if (b == null) return null;
            return Transformations.map(needleRotation, r -> {
                Timber.d("needleRotation");
                return r + b;
            });
        });
        //if mDestBearing.value == null don't show the target icon
        targetIcVisible = Transformations.switchMap(mState, state -> {
            return Transformations.map(mDestinationBearing, bearing -> {
                if (state == CompassStateEnum.SHOW_DESTINATION_AZIMUTH && bearing != null)
                    return true;
                else return false;
            });
        });

        targetIcTranslationX = Transformations.map(destArrowRotation, r -> {
            if (r == null) return null;
            else return (float) (300 * Math.sin(Math.toRadians(r)));
        });
        targetIcTranslationY = Transformations.map(destArrowRotation, r -> {
            if (r == null) return null;
            else return (float) (-300 * Math.cos(Math.toRadians(r)));
        });


        showLocationError = Transformations.switchMap(mState, state -> {
            if (CompassStateEnum.SHOW_DESTINATION_AZIMUTH == state) {
                return Transformations.map(mDestinationBearing, bearing -> {
                    return bearing == null;
                });
            } else {
                return new MutableLiveData<>(false);
            }
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


    public void findButtonClicked(boolean passedPermission, String sLat, String sLon) {
        CompassStateEnum lState = mState.getValue();
        Timber.d("%s button clicked", lState);
        if (lState == CompassStateEnum.COMPASS_ONLY) {
            mState.setValue(CompassStateEnum.EDIT_DESTINATION_LOCATION);
            return;
        } else if (lState == CompassStateEnum.EDIT_DESTINATION_LOCATION) {
            mState.setValue(CompassStateEnum.SHOW_DESTINATION_AZIMUTH);
            initiateAndKeepUpdatingLocation(passedPermission);
            Double dLat = sLat.isEmpty() ? 0.0 : Double.parseDouble(sLat);
            Double dLon = sLon.isEmpty() ? 0.0 : Double.parseDouble(sLon);
            mDestinationCoordinates.setValue(new LocationCoordinates(dLat, dLon));
        } else if (lState == CompassStateEnum.SHOW_DESTINATION_AZIMUTH) {
            mState.setValue(CompassStateEnum.COMPASS_ONLY);
//            mDestinationCoordinates.setValue(null);
            stopLocationUpdates();
        }

    }

    public void provideScreenRotation(int screenRotation) {
        if (mScreenOrientation.getValue() != screenRotation) {
            mScreenOrientation.setValue(screenRotation);
        }
    }

    public void onResume(boolean passedPermission) {
        mManageSensorListenerUseCase.registerListenerForVectorRotationSensor();
        if (hasLocationRequestsPermission && isRequestingLocationUpdates) {
            requestLocationUpdates(passedPermission);
        }
    }


    public void onPause() {
        mManageSensorListenerUseCase.unregisterListenerForVectorRotationSensor();
        if (isRequestingLocationUpdates) {
            stopLocationUpdates();
        }
    }

    private void requestLocationUpdates(boolean passedPermission) {
        isRequestingLocationUpdates = true;
        mRequestAndStopLocationUpdatesUseCase.requestLocationUpdates(passedPermission);
    }

    private void stopLocationUpdates() {
        mRequestAndStopLocationUpdatesUseCase.stopLocationUpdates();
        isRequestingLocationUpdates = false;
    }

    private void initiateAndKeepUpdatingLocation(boolean passedPermission) {
        hasLocationRequestsPermission = true;
        mInitiateLastLocationUseCase.initiateLastLocation(passedPermission);
        requestLocationUpdates(passedPermission);
    }

    public void newButtonClicked(boolean permissionGranted, boolean shouldProvideRationale, String sLat, String sLon) {
        if (permissionGranted) findButtonClicked(permissionGranted, sLat, sLon);
        else if (shouldProvideRationale) mShouldProvideRationale.setValue(true);
        else mRequestLocationPermission.setValue(true);

    }

    public void onRequestPermissionCallback(Boolean isGranted, String sLat, String sLog) {
        if (isGranted) {
            findButtonClicked(isGranted, sLat, sLog);
        } else {
            mShowPermissionDenied.setValue(true);
        }

    }
}