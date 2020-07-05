package com.nowak.wjw.simplecompass.ui.main;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.LocationServices;
import com.google.android.material.snackbar.Snackbar;
import com.nowak.wjw.simplecompass.MyApplication;
import com.nowak.wjw.simplecompass.R;
import com.nowak.wjw.simplecompass.databinding.MainFragmentBinding;
import com.nowak.wjw.simplecompass.di.AppContainer;
import com.nowak.wjw.simplecompass.domain.LocationApiHandler;

import timber.log.Timber;

public class MainFragment extends Fragment implements SensorEventListener {

    private static final String REQUESTING_LOCATION_UPDATES_KEY = "location_updates";
    private MainViewModel mViewModel;
    private MainFragmentBinding mBinding;
    private SensorManager mSensorManager;
    private Sensor mVectorRotationSensor;
    private boolean foundLastLocation;
    private LocationApiHandler mLocationApiHandler;
    private boolean isRequestingLocationUpdates;

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Timber.d("RequestPermission granted");
                    isRequestingLocationUpdates = true;
                    findButtonClicked();
                } else {
                    Timber.d("RequestPermission NOT granted");
                    isRequestingLocationUpdates = false;
                    Snackbar.make(mBinding.main, R.string.location_access_denied,
                            Snackbar.LENGTH_LONG).show();
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
        Timber.d("isRequestingLocationUpdates  :%s", isRequestingLocationUpdates);

        AppContainer appContainer = ((MyApplication) requireActivity().getApplication()).appContainer;
        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        mBinding.setViewModel(mViewModel);
        mViewModel.foundLastLocation.observe(getViewLifecycleOwner(), is -> {
            Timber.d("vm.foundLocation: %s", is);
            foundLastLocation = is;
        });
        Timber.d("foundLastLocation  :%s", foundLastLocation);

        //azimuth feature
        //todo find out scope of sensorManager
        mSensorManager = appContainer.mSensorManager;
        mVectorRotationSensor = appContainer.mVectorRotationSensor;

        //location feature
        mLocationApiHandler = new LocationApiHandler(LocationServices.getFusedLocationProviderClient(getContext()));
        mLocationApiHandler.getLocation().observe(getViewLifecycleOwner(), l -> {
//            Timber.d("newlocation");
            mViewModel.locationChanged(l);
        });


        mBinding.findBt.setOnClickListener((v) -> {
            if (checkPermissions()) {
                Timber.d("button clicked & permission had been granted");
                isRequestingLocationUpdates = true;
                if (!foundLastLocation) configureLocation();
                findButtonClicked();
            } else {
                requestPermissions();
            }
        });

        return mBinding.getRoot();
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(
                getContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale = shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION);

        if (shouldProvideRationale) {
            Timber.d("button clicked shouldShowRequestPermissionRationale");
            Snackbar.make(mBinding.main, R.string.location_access_required,
                    Snackbar.LENGTH_LONG).setAction(R.string.ok_button, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                }
            }).show();
        } else {
            requestPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION);
        }

    }

    private void findButtonClicked() {
        String latS = mBinding.latitudeEt.getText().toString();
        String logS = mBinding.longtidudeEt.getText().toString();
        mViewModel.findButtonClicked(latS, logS);
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("onResume()");
        mSensorManager.registerListener(this, mVectorRotationSensor, SensorManager.SENSOR_DELAY_GAME);
        if (isRequestingLocationUpdates) {
            requestLocationUpdates();
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
        if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
            isRequestingLocationUpdates = savedInstanceState.getBoolean(
                    REQUESTING_LOCATION_UPDATES_KEY);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int mScreenRotation = requireActivity().getWindowManager().getDefaultDisplay().getRotation();
        mViewModel.onSensorChanged(event, mScreenRotation);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void configureLocation() {
        Timber.d("configureLocationUpdates");
        getLastLocation();
        requestLocationUpdates();
    }

    private void getLastLocation() {
        Timber.d("getLastLocation");
        mLocationApiHandler.getLastLocation(requireActivity());
    }

    private void requestLocationUpdates() {
        Timber.d("startLocationUpdates()");
        mLocationApiHandler.requestLocationUpdates();
    }

    private void stopLocationUpdates() {
        mLocationApiHandler.stopLocationUpdates();
    }


}