package com.google.android.gms.samples.vision.face.itracker.activities;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.samples.vision.face.itracker.R;

/**
Class which displays AppArt Logo.
 */
public class StartUp extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_up);

        // Define ActionBar object
        ActionBar actionBar;
        actionBar = getSupportActionBar();

        // Define ColorDrawable object and parse color
        // using parseColor method
        // with color hash code as its parameter
        ColorDrawable colorDrawable
                = new ColorDrawable(this.getColor(R.color.colorPrimaryDark));

        // Set BackgroundDrawable
        actionBar.setBackgroundDrawable(colorDrawable);

        new CountDownTimer(5000, 5000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                startActivity(new Intent(StartUp.this, FaceTrackerActivity.class));
            }
        }.start();
    }
}
