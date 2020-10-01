package com.example.admin.myapplication;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class FlashActivity extends AppCompatActivity {

    private Handler mHandler = new Handler();
    private static int TIME_OUT = 2000; //Time to launch the another activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(FlashActivity.this, MainActivity.class);
                startActivity(intent);
            }
        }, TIME_OUT);

    }
}
