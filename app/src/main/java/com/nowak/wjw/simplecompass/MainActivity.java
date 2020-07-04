package com.nowak.wjw.simplecompass;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.nowak.wjw.simplecompass.ui.main.MainFragment;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate()");
        setContentView(R.layout.main_activity);

        if (!((MyApplication) getApplication()).appContainer.hasRotationSensor()) {
            //todo show message and don't allow to use the app
        }
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow();
        }
    }
}