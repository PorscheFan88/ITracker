package com.google.android.gms.samples.vision.face.itracker.activities.ui.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.support.annotation.Nullable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;

import com.google.android.gms.samples.vision.face.itracker.R;

import java.util.ArrayList;
import java.util.Locale;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private PageViewModel pageViewModel;

    public static PlaceholderFragment newInstance(int index) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);


        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = ViewModelProviders.of(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_voice, container, false);

        final GridLayout grid = root.findViewById(R.id.grid);//Inflate gridview

        int index = getArguments().getInt(ARG_SECTION_NUMBER);//Get current page index

        //Load shared preferences for words
        SharedPreferences pref = getContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        //Array list to store words
        ArrayList<String> words = new ArrayList<>();

        String tag;//Store preference tag
        //Chooses tag based on index
        if (index == 1) {
            tag = "greeting_";
        } else if (index == 2) {
            tag = "action_";
        } else {
            tag = "reaction_";
        }

        boolean run = true;//Tester for loop
        int counter = 1;//Loop counter
        //Loops through preferences
        while (run) {
            String word = pref.getString(tag + counter, null);//Finds word in preferences
            if (word == null) {//If word is null, exit.
                run = false;
                counter = 1;
            } else {//If not null add to list, increment counter
                words.add(word);
                counter++;
            }
        }

        editor.apply();//Apply changes

        final TextToSpeech speech =new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            }
        });
        speech.setLanguage(Locale.CANADA);

        /*
        Add buttons to gridview
         */
        grid.removeAllViews();
        int total = words.size();//Total size is equal to number of words
        //Getting row and column counts
        int column = 2;
        int row = total / column;
        grid.setColumnCount(column);
        grid.setRowCount(row + 1);
        for(int i = 0; i < total; i++)//Looping through the columns
        {
           final Button btn = new Button(getContext());

            GridLayout.LayoutParams param =new GridLayout.LayoutParams();
            param.height = 0;
            param.width = 0;
            param.rightMargin = 5;
            param.topMargin = 5;
            param.setGravity(Gravity.CENTER);
            param.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            param.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            btn.setBackgroundResource(R.drawable.button_selector);
            btn.setTextColor(getContext().getResources().getColor(R.color.white));
            btn.setLayoutParams (param);
            btn.setAllCaps(false);
            btn.setText(words.get(i));
            grid.addView(btn);

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    speech.speak(btn.getText().toString(),TextToSpeech.QUEUE_FLUSH, null);
                }
            });

        }
        return root;
    }

}