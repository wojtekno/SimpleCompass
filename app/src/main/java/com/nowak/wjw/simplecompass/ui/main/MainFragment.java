package com.nowak.wjw.simplecompass.ui.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.nowak.wjw.simplecompass.MyApplication;
import com.nowak.wjw.simplecompass.R;
import com.nowak.wjw.simplecompass.databinding.MainFragmentBinding;
import com.nowak.wjw.simplecompass.di.AppContainer;

import timber.log.Timber;

public class MainFragment extends Fragment implements SensorEventListener {

    private static final String REQUESTING_LOCATION_UPDATES_KEY = "location_updates";
    private MainViewModel mViewModel;
    private MainFragmentBinding mBinding;
    private SensorManager mSensorManager;
    private Sensor mVectorRotationSensor;
    private FusedLocationProviderClient mFusedLocationClient;
    private MutableLiveData<Location> mLocation = new MutableLiveData<>();
    private LocationRequest mLocationRequest;
    private boolean isRequestingLocationUpdates;
    private boolean mIsLocationRequestSetUp;
    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }
            for (Location location : locationResult.getLocations()) {
                // Update UI with location data
                // ...
                Timber.d("locationCallback onLocationResult");
                // TODO
                mViewModel.locationChanged(location);
            }
        }
    };


    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                    Timber.d("RequestPermission granted");
                    isRequestingLocationUpdates = true;
                    findButtonClicked();
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                    Timber.d("RequestPermission NOT granted");
                    isRequestingLocationUpdates = false;
                    Toast.makeText(getContext(), "You dind't let us track your position, we cannot find your destination", Toast.LENGTH_SHORT).show();
                }
            });

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Timber.d("onCreateView()");
        mBinding = DataBindingUtil.inflate(inflater, R.layout.main_fragment, container, false);
        mBinding.setLifecycleOwner(getViewLifecycleOwner());

        updateValuesFromBundle(savedInstanceState);

        AppContainer appContainer = ((MyApplication) requireActivity().getApplication()).appContainer;
        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        mBinding.setViewModel(mViewModel);

        //azimuth feature
        //todo find out scope of sensorManager
        mSensorManager = appContainer.mSensorManager;
        mVectorRotationSensor = appContainer.mVectorRotationSensor;

        //location feature
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

        mBinding.findBt.setOnClickListener((v) -> {
            if (ContextCompat.checkSelfPermission(
                    getContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {
                // You can use the API that requires the permission.
//                performAction(...);
                Timber.d("button clicked permission granted");
                isRequestingLocationUpdates = true;
                if (!mIsLocationRequestSetUp) configureLocation();
                findButtonClicked();
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                //todo checkout permissionGroup.

                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected. In this UI,
                // include a "cancel" or "no thanks" button that allows the user to
                // continue using your app without granting the permission.
                // TODO: show a snackBar
                //  https://github.com/android/permissions-samples/tree/master/RuntimePermissionsBasic
                Timber.d("button clicked shouldShowRequestPermissionRationale");

                Toast.makeText(getContext(), "We need your permission to track your position", Toast.LENGTH_SHORT).show();
//                showInContextUI(...);
            } else {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(
                        Manifest.permission.ACCESS_FINE_LOCATION);
            }
        });

        return mBinding.getRoot();
    }

    private void findButtonClicked() {

        double lat = Double.parseDouble(mBinding.latitudeEt.getText().toString());
        double lon = Double.parseDouble(mBinding.longtidudeEt.getText().toString());
        mViewModel.findClicked(lat, lon);
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("onResume()");
        mSensorManager.registerListener(this, mVectorRotationSensor, SensorManager.SENSOR_DELAY_GAME);
        if (isRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Timber.d("onPause()");
        mSensorManager.unregisterListener(this);
        stopLocationUpdates();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY,
                isRequestingLocationUpdates);
        super.onSaveInstanceState(outState);

    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        // Update the value of requestingLocationUpdates from the Bundle.
        if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
            isRequestingLocationUpdates = savedInstanceState.getBoolean(
                    REQUESTING_LOCATION_UPDATES_KEY);
        }
        // Update UI to match restored state
//        updateUI();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int mScreenRotation = requireActivity().getWindowManager().getDefaultDisplay().getRotation();
        mViewModel.onSensorChanged(event, mScreenRotation);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // TODO: implement SettingsClient
    //  https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient

    private void configureLocation() {
        Timber.d("configureLocationUpdates");
//        getLastLocation();
        startLocationUpdates();
        mIsLocationRequestSetUp = true;
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        Timber.d("getLastLocation onSuccess");
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            mLocation.setValue(location);
                            // Logic to handle location object
                        }
                    }
                });
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        Timber.d("startLocationUpdates()");
        createLocationRequest();
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper());
    }

    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }


}