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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.nowak.wjw.simplecompass.MyApplication;
import com.nowak.wjw.simplecompass.R;
import com.nowak.wjw.simplecompass.databinding.MainFragmentBinding;
import com.nowak.wjw.simplecompass.di.AppContainer;

import timber.log.Timber;

public class MainFragment extends Fragment implements SensorEventListener {

    private MainViewModel mViewModel;
    private MainFragmentBinding mBinding;
    private SensorManager mSensorManager;
    private Sensor mVectorRotationSensor;

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                    findButtonClicked();
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.

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

        AppContainer appContainer = ((MyApplication) requireActivity().getApplication()).appContainer;
        //todo find out scope of sensorManager
        mSensorManager = appContainer.mSensorManager;
        mVectorRotationSensor = appContainer.mVectorRotationSensor;
        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        mBinding.setViewModel(mViewModel);

        mBinding.findBt.setOnClickListener((v) -> {
            if (ContextCompat.checkSelfPermission(
                    getContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {
                // You can use the API that requires the permission.
//                performAction(...);
                findButtonClicked();
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                //todo checkout permissionGroup.

                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected. In this UI,
                // include a "cancel" or "no thanks" button that allows the user to
                // continue using your app without granting the permission.
                // TODO: show a snackBar
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
    }

    @Override
    public void onPause() {
        super.onPause();
        Timber.d("onPause()");
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int mScreenRotation = requireActivity().getWindowManager().getDefaultDisplay().getRotation();
        mViewModel.onSensorChanged(event, mScreenRotation);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}