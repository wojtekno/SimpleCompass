package com.nowak.wjw.simplecompass.domain;

import android.annotation.SuppressLint;
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
    private MutableLiveData<Boolean> foundLastLocation = new MutableLiveData<>();
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

    @SuppressLint("MissingPermission")
    public void getLastLocation(Activity activity) {
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        Timber.d("getLastLocation onSuccess");
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            mLocation.setValue(location);
                            foundLastLocation.setValue(true);
//                            mViewModel.locationChanged(location);
                            // Logic to handle location object
                        }
                    }
                });
    }

    public LiveData<Boolean> getFoundLastLocation() {
        return foundLastLocation;
    }

    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @SuppressLint("MissingPermission")
    public void startLocationUpdates() {
        Timber.d("startLocationUpdates()");
        createLocationRequest();
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, this, Looper.getMainLooper());
    }

    public void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(this);
    }
}
