package com.google.android.gms.samples.vision.face.itracker.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.samples.vision.face.itracker.R;
import com.google.android.gms.samples.vision.face.itracker.activities.ui.main.SectionsPagerAdapter;

import java.util.ArrayList;

public class Voice extends AppCompatActivity {

    private String TAG = "Voice";
    //ArrayLists to store words
    private ArrayList<String> greetings = new ArrayList<>();
    private ArrayList<String> actions = new ArrayList<>();
    private ArrayList<String> reactions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_voice);
        final SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        final ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        //Load shared preferences for words
        final SharedPreferences pref = getSharedPreferences("MyPref", 0); // 0 - for private mode

        greetings = SharedPreferenceWords.toArrayList(pref, "greeting_");
        actions = SharedPreferenceWords.toArrayList(pref, "action_");
        reactions = SharedPreferenceWords.toArrayList(pref, "reaction_");

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Context context = Voice.this;

                final AlertDialog.Builder builder = new AlertDialog.Builder(context);

                LayoutInflater inflater = LayoutInflater.from(context);
                final View dialogView= inflater.inflate(R.layout.add_word_dialog, null);
                //EditText for entered words
                final EditText edtWord = dialogView.findViewById(R.id.edtAddWord);

                builder.setView(dialogView);
                final AlertDialog ad = builder.create();
                ad.show();
                //Add word button is pressed.
                dialogView.findViewById(R.id.btnAddWord).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String word = edtWord.getText().toString();
                        //Check if word is not of zero length
                        if (word.trim().length() <= 0) {
                            Toast.makeText(context,getString(R.string.incorrect_input), Toast.LENGTH_LONG).show();
                        } else {
                            int index = viewPager.getCurrentItem();//Get current page number
                            //Choose tag beginning
                            if (index == 0) {
                                greetings.add(word);
                                SharedPreferenceWords.toSharedPreference(pref,"greeting_",greetings);
                            } else if (index == 1) {
                                actions.add(word);
                                SharedPreferenceWords.toSharedPreference(pref,"action_",actions);
                            } else {
                                reactions.add(word);
                                SharedPreferenceWords.toSharedPreference(pref,"reaction_",reactions);
                            }

                            Toast.makeText(context,getString(R.string.word_added), Toast.LENGTH_LONG).show();
                            //Update GridLayout and close dialog
                            viewPager.setAdapter(sectionsPagerAdapter);
                            viewPager.setCurrentItem(index);
                            ad.cancel();
                        }
                    }
                });

            }
        });
    }

}