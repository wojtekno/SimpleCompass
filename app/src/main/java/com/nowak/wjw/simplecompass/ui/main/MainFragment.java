package com.nowak.wjw.simplecompass.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.nowak.wjw.simplecompass.R;
import com.nowak.wjw.simplecompass.databinding.MainFragmentBinding;

public class MainFragment extends Fragment {

    private MainViewModel mViewModel;
    private MainFragmentBinding mbinding;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mbinding = DataBindingUtil.inflate(inflater, R.layout.main_fragment, container, false);
        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        return mbinding.getRoot();
    }

}