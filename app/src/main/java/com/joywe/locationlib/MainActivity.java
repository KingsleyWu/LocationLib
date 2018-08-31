package com.joywe.locationlib;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DebugUtils;
import android.util.Log;

import com.joywe.locationlibrary.IpToCityManger;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String cityNameByIpUrl = IpToCityManger.getInstance().getCityNameByIpUrl();
        //Log.d(TAG, "onCreate: ");
    }
}
