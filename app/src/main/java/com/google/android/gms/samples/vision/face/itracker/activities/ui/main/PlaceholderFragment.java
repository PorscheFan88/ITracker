package com.google.android.gms.samples.vision.face.itracker.activities.ui.main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.samples.vision.face.itracker.R;
import com.google.android.gms.samples.vision.face.itracker.activities.SharedPreferenceWords;

import java.util.ArrayList;
import java.util.Locale;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    //ArrayLists to store words
    private ArrayList<String> greetings = new ArrayList<>();
    private ArrayList<String> actions = new ArrayList<>();
    private ArrayList<String> reactions = new ArrayList<>();

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
            @NonNull LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_voice, container, false);

        //Load shared preferences for words
        final SharedPreferences pref = getContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        //Populating arrayLists with words
        greetings = SharedPreferenceWords.toArrayList(pref, "greeting_");
        actions = SharedPreferenceWords.toArrayList(pref, "action_");
        reactions = SharedPreferenceWords.toArrayList(pref, "reaction_");

        final GridLayout grid = root.findViewById(R.id.grid);//Inflate gridview

        int index = getArguments().getInt(ARG_SECTION_NUMBER);//Get current page index

        //Array list to store words
        final ArrayList<String> words;
        final String tag;
        //Chooses correct set of words
        if (index == 1) {
           words = greetings;
           tag = "greeting_";
        } else if (index == 2) {
            words = actions;
            tag = "action_";
        } else {
            words = reactions;
            tag = "reaction_";
        }

        //Speech engine
        final TextToSpeech speech =new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            }
        });
        speech.setLanguage(Locale.CANADA);//Language

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
            //Click listener
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Speak
                    speech.speak(btn.getText().toString(),TextToSpeech.QUEUE_FLUSH, null);
                }
            });
            //On a long click, delete word
            btn.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                    final LayoutInflater inflater = LayoutInflater.from(getContext());
                    final View dialogView= inflater.inflate(R.layout.delete_word_dialog, null);

                    builder.setView(dialogView);
                    final AlertDialog ad = builder.create();
                    ad.show();

                    dialogView.findViewById(R.id.btnDeleteWord).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            words.remove(btn.getText().toString());//Remove word from list
                            SharedPreferenceWords.toSharedPreference(pref,tag,words);//Update preferences
                            ad.cancel();//Close dialog

                            Toast.makeText(getContext(), getString(R.string.word_deleted), Toast.LENGTH_LONG).show();

                            grid.removeView(btn);//Remove UI Button. (Real word has been removed already)

                        }
                    });
                    return false;
                }
            });

        }
        return root;
    }

}