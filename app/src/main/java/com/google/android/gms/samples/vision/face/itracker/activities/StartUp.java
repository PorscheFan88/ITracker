package com.google.android.gms.samples.vision.face.itracker.activities;

import android.content.Intent;
import android.content.SharedPreferences;
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


        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();

        boolean opened = pref.getBoolean("init", false); //getting String to check if app has been opened before

        if (!opened) {
            editor.putString("greeting_1", getString(R.string.greeting_1)); // Storing string
            editor.putString("greeting_2", getString(R.string.greeting_2));
            editor.putString("action_1", getString(R.string.action_1));
            editor.putString("action_2", getString(R.string.action_2));
            editor.putString("action_3", getString(R.string.action_3));
            editor.putString("reaction_1", getString(R.string.reaction_1));
            editor.putString("reaction_2", getString(R.string.reaction_2));
            editor.putString("reaction_3", getString(R.string.reaction_3));
            editor.putString("reaction_4", getString(R.string.reaction_4));
            editor.putBoolean("init", true);
        }

        editor.commit(); // commit changes

        new CountDownTimer(3000, 3000) {
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
