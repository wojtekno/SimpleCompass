package com.nowak.wjw.simplecompass.ui.main;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

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
import com.nowak.wjw.simplecompass.R;
import com.nowak.wjw.simplecompass.databinding.MainFragmentBinding;
import com.nowak.wjw.simplecompass.location.LocationApiHandler;
import com.nowak.wjw.simplecompass.sensors.SensorHandler;

import timber.log.Timber;

//todo You implement `SensorEventListener` in `MainFragment` which couples code.
public class MainFragment extends Fragment {

    private static final String REQUESTING_LOCATION_UPDATES_KEY = "location_updates";
    private MainViewModel mViewModel;
    private MainFragmentBinding mBinding;
    private SensorHandler mSensorHandler;
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

        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        mBinding.setViewModel(mViewModel);
        mViewModel.getmFoundLastLocation().observe(getViewLifecycleOwner(), is -> {
            if (!is.getHasBeenHandled()) {
                Timber.d("vm.foundLocation: %s", is.peekContent());
                foundLastLocation = is.getContentIfNotHandled();
            }
        });
        Timber.d("foundLastLocation  :%s", foundLastLocation);
        mViewModel.hideKeyBoard.observe(getViewLifecycleOwner(), hide -> {
            if (hide) {
                hideKeyboard();
            }
        });

        mViewModel.stopLocationUpdates().observe(getViewLifecycleOwner(), s -> {
            Timber.d("stoplocationUpdates: %s", s);
            //todo There are multiple constructs where inside some `LiveData` observer you have a single-branched conditional instruction.
            // This code would greatly benefit from `filter()` method known from libraries like RxJava.
            if (s) stopLocationUpdates();
        });

        mViewModel.startLocationUpdates().observe(getViewLifecycleOwner(), s -> {
            Timber.d("startlocationUpdates: %s", s);
            if (s) requestLocationUpdates();
        });


        //azimuth feature
        //todo find out scope of sensorManager
        mSensorHandler = new SensorHandler((SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE));
        mSensorHandler.getSensorEvent().observe(getViewLifecycleOwner(), event -> {
            int mScreenRotation = requireActivity().getWindowManager().getDefaultDisplay().getRotation();
            mViewModel.onSensorChanged(event, mScreenRotation);
        });


        //location feature
        // todo You tend to use `getContext()` which may lead to `NullPointerException`. The `requireContext()` method would provide a better insight into what happened in such case.
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

    private void hideKeyboard() {
        // todo You tend to use `getContext()` which may lead to `NullPointerException`. The `requireContext()` method would provide a better insight into what happened in such case.
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getRootView().getWindowToken(), 0);
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(
                // todo You tend to use `getContext()` which may lead to `NullPointerException`. The `requireContext()` method would provide a better insight into what happened in such case.
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
        mSensorHandler.registerListenerForVectorRotationSensor();
        if (isRequestingLocationUpdates) {
            requestLocationUpdates();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Timber.d("onPause()");
        mSensorHandler.unregisterListenerForVectorRotationSensor();
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
        Timber.d("stopLocationUpdates");
        mLocationApiHandler.stopLocationUpdates();
    }


}