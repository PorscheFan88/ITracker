package com.google.android.gms.samples.vision.face.itracker.activities;

import android.content.SharedPreferences;

import java.util.ArrayList;

public class SharedPreferenceWords {
    /*
   Method populates word list
    */
    public static ArrayList<String> toArrayList(SharedPreferences pref, String tag) {
        ArrayList<String> list = new ArrayList<>();

        int counter = 1;//Loop counter
        //Loops through preferences
        while (true) {
            String word = pref.getString(tag + counter, null);//Finds word in preferences
            if (word == null) {//If word is null, exit.
                return list;
            } else {//If not null add to list, increment counter
                counter++;
                list.add(word);
            }
        }
    }

    /*
    Method moves list to shared preferences
     */
    public static void toSharedPreference(SharedPreferences pref, String tag, ArrayList<String> list) {
        SharedPreferences.Editor editor = pref.edit();//Get preferences

        /*
        Clear old preferences!
         */
        int counter = 1;//Loop counter
        boolean tester = true;
        //Loops through preferences
        while (tester) {
            String word = pref.getString(tag + counter, null);//Finds word in preferences
            if (word == null) {//If word is null, exit.
                tester = false;
                counter = 1;
            } else {//If not null add to list, increment counter
                counter++;
                editor.remove(tag + counter);//Removes word
            }
        }

        //Repopulate with list words!
        for (int i = 0; i < list.size(); i++) {
            editor.putString(tag + (i+1), list.get(i));
        }

        editor.commit();
    }

    /*
  Gets the number of words under each category
   */
    public static int getPrefLength(SharedPreferences pref, String tag) {
        int counter = 1;//Loop counter
        //Loops through preferences
        while (true) {
            String word = pref.getString(tag + counter, null);//Finds word in preferences
            if (word == null) {//If word is null, exit.
                return counter;
            } else {//If not null add to list, increment counter
                counter++;
            }
        }
    }
}
