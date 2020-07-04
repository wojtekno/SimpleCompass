package com.nowak.wjw.simplecompass.ui.main;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
        mSensorManager = appContainer.mSensorManager;
        mVectorRotationSensor = appContainer.mVectorRotationSensor;
        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        mBinding.setViewModel(mViewModel);

        return mBinding.getRoot();
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