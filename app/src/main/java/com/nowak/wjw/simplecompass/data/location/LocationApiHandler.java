package com.nowak.wjw.simplecompass.data.location;

import android.location.Location;
import android.os.Looper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.nowak.wjw.simplecompass.data.LocationCoordinates;

import org.jetbrains.annotations.NotNull;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import timber.log.Timber;

//todo There’s no clear separation of architecture layer. You made a `domain` package, but inside are Android SDK dependencies.
public class LocationApiHandler extends LocationCallback {
    public static final String SPECIAL_LOCATION_PROVIDER_WHEN_NO_LAST_FOUND = "special_location_zero_provider";
    private final int LOCATION_REQUEST_INTERVAL = 10000;
    private final int LOCATION_REQUEST_FASTEST_INTERVAL = 5000;
    private LocationRequest mLocationRequest;
    private BehaviorSubject<Location> locationBehaviorSubject = BehaviorSubject.create();
    private FusedLocationProviderClient mFusedLocationClient;

    public LocationApiHandler(FusedLocationProviderClient mFusedLocationProviderClient) {
        this.mFusedLocationClient = mFusedLocationProviderClient;
    }


    @Override
    public void onLocationResult(LocationResult locationResult) {
        super.onLocationResult(locationResult);
        Timber.d("onLocationResult %s %s", locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
        locationBehaviorSubject.onNext(locationResult.getLastLocation());
    }


    /**
     * Call requires permission which may be rejected by user:
     * code should explicitly check to see if permission is available (with checkPermission) or explicitly handle a potential SecurityException
     *
     * @throws SecurityException
     */
    public void initiateLastLocation() throws SecurityException {
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        Timber.d("getLastLocation onSuccess %s, %s", location.getLatitude(), location.getLongitude());
                        locationBehaviorSubject.onNext(location);
                    } else {
                        Timber.d("getLastLocation onSuccess location null");
                        locationBehaviorSubject.onNext(new Location(SPECIAL_LOCATION_PROVIDER_WHEN_NO_LAST_FOUND));
                    }
                });
    }


    /**
     * Call requires permission which may be rejected by user:
     * code should explicitly check to see if permission is available (with checkPermission) or explicitly handle a potential SecurityException
     *
     * @param passedPermission
     * @throws SecurityException
     */
    public void requestLocationUpdates(boolean passedPermission) throws SecurityException {
        // TODO: implement SettingsClient
        //  https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        if (mLocationRequest == null) {
            createLocationRequest();
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, this, Looper.getMainLooper());
    }

    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(LOCATION_REQUEST_INTERVAL);
        mLocationRequest.setFastestInterval(LOCATION_REQUEST_FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    public void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(this);
    }


    public Observable<Float> getBearingTo(@NotNull LocationCoordinates destCoordinates) {
        Location destination;
        if (destCoordinates == null) {
            destination = new Location("null");
        } else {
            destination = new Location("destCoordinates");
            destination.setLatitude(destCoordinates.getLatitude());
            destination.setLongitude(destCoordinates.getLongitude());
        }
        return locationBehaviorSubject
                .filter(location -> destination.getProvider().equals("destCoordinates"))
                .map(location -> {
                    Timber.d("mapping locationSubject %s %s", location.getLatitude(), location.getLongitude());
                    return location.bearingTo(destination);
                });
    }

}
