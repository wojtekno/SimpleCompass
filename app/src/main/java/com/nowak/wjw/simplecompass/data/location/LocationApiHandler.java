package com.nowak.wjw.simplecompass.data.location;

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

//todo There’s no clear separation of architecture layer. You made a `domain` package, but inside are Android SDK dependencies.
public class LocationApiHandler extends LocationCallback {
    public static final String SPECIAL_LOCATION_PROVIDER_WHEN_NO_LAST_FOUND = "special_location_zero_provider";
    private MutableLiveData<Location> mLocation = new MutableLiveData<>();
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;

    public LocationApiHandler(FusedLocationProviderClient mFusedLocationProviderClient) {
        this.mFusedLocationClient = mFusedLocationProviderClient;
    }

    @Override
    public void onLocationResult(LocationResult locationResult) {
        super.onLocationResult(locationResult);
//        Timber.d("onLocationResult %s %s", locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
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
    public void initiateLastLocation() throws SecurityException {
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            Timber.d("getLastLocation onSuccess %s, %s", location.getLatitude(), location.getLongitude());
                            mLocation.setValue(location);
                        } else {
                            mLocation.setValue(new Location(SPECIAL_LOCATION_PROVIDER_WHEN_NO_LAST_FOUND));
                        }
                    }
                });
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
        if (mLocationRequest == null) {
            createLocationRequest();
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, this, Looper.getMainLooper());
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
}
