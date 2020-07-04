package com.nowak.wjw.simplecompass;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.nowak.wjw.simplecompass.ui.main.MainFragment;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate()");
        setContentView(R.layout.main_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow();
        }
    }
}