package com.nowak.wjw.simplecompass.domain;

import android.app.Activity;
import android.location.Location;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnSuccessListener;

import timber.log.Timber;

public class LocationApiHandler extends LocationCallback {
    private MutableLiveData<Location> mLocation = new MutableLiveData<>();
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;

    public LocationApiHandler(FusedLocationProviderClient mFusedLocationProviderClient) {
        this.mFusedLocationClient = mFusedLocationProviderClient;
    }

    @Override
    public void onLocationResult(LocationResult locationResult) {
        super.onLocationResult(locationResult);
        mLocation.setValue(locationResult.getLastLocation());
    }

    public LiveData<Location> getLocation() {
        return mLocation;
    }

    /**
     * Call requires permission which may be rejected by user:
     * code should explicitly check to see if permission is available (with checkPermission) or explicitly handle a potential SecurityException
     *
     * @throws SecurityException
     */
    public void getLastLocation(Activity activity) throws SecurityException {
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        Timber.d("getLastLocation onSuccess");
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            mLocation.setValue(location);
                        }
                    }
                });
    }

    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(this);
    }

    /**
     * Call requires permission which may be rejected by user:
     * code should explicitly check to see if permission is available (with checkPermission) or explicitly handle a potential SecurityException
     *
     * @throws SecurityException
     */
    public void requestLocationUpdates() throws SecurityException {
        // TODO: implement SettingsClient
        //  https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        createLocationRequest();
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, this, Looper.getMainLooper());
    }
}
