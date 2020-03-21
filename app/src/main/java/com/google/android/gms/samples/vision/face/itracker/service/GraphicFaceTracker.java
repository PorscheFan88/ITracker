package com.google.android.gms.samples.vision.face.itracker.service;

import android.content.Context;
import android.view.Display;
import android.view.WindowManager;

import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;

/**
 * Face tracker for each detected individual. This maintains a face graphic within the app's
 * associated face overlay.
 */
public class GraphicFaceTracker extends Tracker<Face> {
    private String TAG = "GraphicFaceTracker";

    private volatile Face mFace;//Users face
    private float cx2, cy2,//Second floats for calculating deltas
            newX = 0, newY = 0;//The final X and Y coordinates after sensitivity effect
    private float sensitivity = 8;//Controls how fast the cursor moves. Default 7.
    private CursorService cursorService;
    private int clickC = 0;//Click counter avoids rapid click firing

    GraphicFaceTracker(CursorService cs) {
        cursorService = cs;
    }
    /*
    Updates the newX and newY variables for the face.
   */
    public void getFaceCoord() {
        Face face = mFace;
        if (face != null) {
            //Get all landmarks of the face
            for (Landmark landmark : face.getLandmarks()) {
                if ((landmark.getType() == Landmark.NOSE_BASE)) {//Track nose
                    //Get display width
                    WindowManager window = (WindowManager) cursorService.getSystemService(Context.WINDOW_SERVICE);
                    Display display = window.getDefaultDisplay();
                    int width = display.getWidth();

                    float cx = width - landmark.getPosition().x;//X will be the opposite of the landmark position
                    float cy = landmark.getPosition().y;

                    //If final positions are zero, then face is being recognized for the first time
                    //Set final positions to current face location
                    if (cx2 == 0 && cy2 == 0) {
                        cx2 = cx;
                        cy2 = cy;
                    }
                    //Calculate change in position
                    float deltaX = cx - cx2;
                    float deltaY = cy - cy2;
                    //If newX is zero, then the face is being recognized for the first time
                    //Set the newX and newY to current face location
                    if (newX != 0) {
                        //Update newX & newY values using delta and sensitivity
                        newX = newX + deltaX * sensitivity;
                        newY = newY + deltaY * sensitivity;
                    } else {
                        newX = cx;
                        newY = cy;
                    }
                    //Change final position to current position
                    cx2 = cx;
                    cy2 = cy;
                }

            }
        }
    }
    /*
    Sets the sensitivity of the cursor
     */
    public void setSensitivity(float sensitivity) {
        this.sensitivity = sensitivity;
    }


    /*
    Updates the cursor actions based on the facial movements.
     */
    @Override
    public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
        mFace = face;
        getFaceCoord();//Update the position of the face

        //@TODO find a facial movement for toolbar controls.
        if (face.getIsSmilingProbability() > 0.5) {
            if (clickC < 1) {//Perform individual click
                cursorService.click();
            } else if (clickC > 5) {//Perform long click
                cursorService.longClick();
            }
            clickC++;
        } else {
            cursorService.onMouseMove((int) newX, (int) newY);
            clickC = 0;
        }
    }

}
