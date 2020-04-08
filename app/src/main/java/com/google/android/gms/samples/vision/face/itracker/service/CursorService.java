package com.google.android.gms.samples.vision.face.itracker.service;

import android.accessibilityservice.AccessibilityService;
import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;

import com.google.android.gms.samples.vision.face.itracker.R;
import com.google.android.gms.samples.vision.face.itracker.ui.camera.CameraSourcePreview;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.IOException;

import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;

public class CursorService extends AccessibilityService {
    private static final String TAG = CursorService.class.getName();
    private FrameLayout cursorLayout;

    private CameraSourcePreview mPreview;
    private GraphicFaceTracker mFaceTracker;
    private CameraSource mCameraSource = null;
    private WindowManager wm;
    private WindowManager.LayoutParams cursorLP, previewLP;
    private AccessibilityActions accessibilityActions;


    @Override
    protected void onServiceConnected() {
        accessibilityActions = new AccessibilityActions(CursorService.this);

        // Create an overlay and display the action bar
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        cursorLayout = new FrameLayout(this);
        cursorLP = new WindowManager.LayoutParams();
        cursorLP.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        cursorLP.format = PixelFormat.TRANSLUCENT;
        cursorLP.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | FLAG_LAYOUT_NO_LIMITS;
        cursorLP.width = WindowManager.LayoutParams.WRAP_CONTENT;
        cursorLP.height = WindowManager.LayoutParams.WRAP_CONTENT;
        cursorLP.gravity = Gravity.START|Gravity.TOP;
        LayoutInflater inflater = LayoutInflater.from(this);
        inflater.inflate(R.layout.cursor, cursorLayout);

        FrameLayout previewLayout = new FrameLayout(this);
        previewLP = new WindowManager.LayoutParams();
        previewLP.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        previewLP.format = PixelFormat.TRANSLUCENT;
        previewLP.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        previewLP.width = WindowManager.LayoutParams.WRAP_CONTENT;
        previewLP.height = WindowManager.LayoutParams.WRAP_CONTENT;
        previewLP.gravity = Gravity.TOP;
        inflater.inflate(R.layout.preview, previewLayout);

        int mHeight= this.getResources().getDisplayMetrics().heightPixels;

        cursorLP.y = mHeight/2;

        wm.addView(previewLayout, previewLP);
        wm.addView(cursorLayout, cursorLP);

        mPreview = (CameraSourcePreview) previewLayout.findViewById(R.id.camView);//Getting camera preview from floating layout

        createCameraSource();
    }

    public int getX() {
        return cursorLP.x;
    }

    public int getY() {
        return cursorLP.y;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onInterrupt() {

    }

    private void createCameraSource() {

        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setProminentFaceOnly(true)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }

        mCameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30.0f)
                .build();

        startCameraSource();
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    public void click() {
        accessibilityActions.click();
    }

    public Activity getActivity() {
        return getActivity();
    }

    public void longClick(){
        accessibilityActions.longClick();
    }

    public void goHome(){
        accessibilityActions.goHome(getBaseContext());
    }

    public void goBack() {}//@TODO go back

    public void goRecents() {}//@TODO go to recent windows

    public void onMouseMove(int x, int y) {

        cursorLP.x = x;
        cursorLP.y = y;

        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                wm.updateViewLayout(cursorLayout, cursorLP);
            }
        });

    }
    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    public class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            mFaceTracker = new GraphicFaceTracker(CursorService.this);
            return mFaceTracker;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

}
