package com.nowak.wjw.simplecompass.ui.main;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
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

import com.google.android.material.snackbar.Snackbar;
import com.nowak.wjw.simplecompass.MyApplication;
import com.nowak.wjw.simplecompass.R;
import com.nowak.wjw.simplecompass.databinding.MainFragmentBinding;

import timber.log.Timber;

public class MainFragment extends Fragment {

    private MainFragmentBinding mBinding;
    private MainViewModel mViewModel;
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                String latS = mBinding.latitudeEt.getText().toString();
                String logS = mBinding.longtidudeEt.getText().toString();
                mViewModel.onRequestPermissionCallback(isGranted, latS, logS);
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

        MainViewModelFactory mainViewModelFactory = ((MyApplication) getActivity().getApplication()).appContainer.mainViewModelFactory();
        mViewModel = new ViewModelProvider(this, mainViewModelFactory).get(MainViewModel.class);
        mBinding.setViewModel(mViewModel);
        setVmObservers();

        mBinding.findBt.setOnClickListener((v) -> {
            String latS = mBinding.latitudeEt.getText().toString();
            String logS = mBinding.longtidudeEt.getText().toString();
            boolean hasPermissions = checkPermissions();
            boolean shouldProvideRationale = shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION);

            mViewModel.newButtonClicked(hasPermissions, shouldProvideRationale, latS, logS);
        });

        return mBinding.getRoot();
    }

    private void setVmObservers() {
        mViewModel.needScreenOrientation.observe(getViewLifecycleOwner(), isNeeded -> {
            if (isNeeded) {
                int mScreenRotation = requireActivity().getWindowManager().getDefaultDisplay().getRotation();
                mViewModel.provideScreenRotation(mScreenRotation);
            }
        });

        mViewModel.hideKeyBoard.observe(getViewLifecycleOwner(), hide -> {
            if (hide) {
                hideKeyboard();
            }
        });

        mViewModel.shouldProvideRationale.observe(getViewLifecycleOwner(), booleanEvent -> {
            if (booleanEvent.getContentIfNotHandled() != null && booleanEvent.peekContent()) {
                Timber.d("button clicked shouldShowRequestPermissionRationale");
                Snackbar.make(mBinding.main, R.string.location_access_required,
                        Snackbar.LENGTH_LONG).setAction(R.string.ok_button, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        requestLocationPermission();
                    }
                }).show();
            }
        });

        mViewModel.requestLocationPermission.observe(getViewLifecycleOwner(), booleanEvent -> {
            if (booleanEvent.getContentIfNotHandled() != null && booleanEvent.peekContent())
                requestLocationPermission();
        });

        mViewModel.showPermissionDenied.observe(getViewLifecycleOwner(), booleanEvent -> {
            if (booleanEvent.getContentIfNotHandled() != null && booleanEvent.peekContent()) {
                Timber.d("RequestPermission NOT granted");
                Snackbar.make(mBinding.main, R.string.location_access_denied,
                        Snackbar.LENGTH_LONG).show();
            }
        });
    }


    private void requestLocationPermission() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
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


    @Override
    public void onResume() {
        super.onResume();
        Timber.d("onResume()");
        mViewModel.onResume(checkPermissions());
    }


    @Override
    public void onPause() {
        super.onPause();
        Timber.d("onPause()");
        mViewModel.onPause();
    }

}