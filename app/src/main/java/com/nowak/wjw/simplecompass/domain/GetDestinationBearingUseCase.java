package com.nowak.wjw.simplecompass.domain;

import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.nowak.wjw.simplecompass.data.LocationCoordinates;
import com.nowak.wjw.simplecompass.data.location.LocationApiHandler;

import timber.log.Timber;

public class GetDestinationBearingUseCase {

    private LocationApiHandler mLocationApiHandler;

    public GetDestinationBearingUseCase(LocationApiHandler locationApiHandler) {
        mLocationApiHandler = locationApiHandler;
    }

    /**
     * Calculates bearing to a destination point
     *
     * @param destCoordinates coordinates of a destination
     * @return bearing represented as an angle or null when current destination hasn't been established
     */
    public LiveData<Float> getMyDestinationBearing(LocationCoordinates destCoordinates) {
        Timber.d("getMyDestinationBearing %s %s ", destCoordinates.getLatitude(), destCoordinates.getLongitude());
        LiveData<Location> currentLocation = mLocationApiHandler.getLocation();
        Location destination = new Location("special object");
        destination.setLatitude(destCoordinates.getLatitude());
        destination.setLongitude(destCoordinates.getLongitude());
        return Transformations.map(currentLocation, l -> {
            if (l.getProvider() == LocationApiHandler.SPECIAL_LOCATION_PROVIDER_WHEN_NO_LAST_FOUND) {
                return null;
            }
            float bearingTo = l.bearingTo(destination);
            Timber.d("returning bearing %s", bearingTo);
            return bearingTo;
        });
    }

}
