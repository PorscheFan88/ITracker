package com.google.android.gms.samples.vision.face.itracker;

import android.accessibilityservice.AccessibilityService;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Rect;

import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.samples.vision.face.itracker.ui.camera.CameraSourcePreview;
import com.google.android.gms.samples.vision.face.itracker.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class CursorAccessibilityService extends Service {
    private static final String TAG = CursorAccessibilityService.class.getName();

    private CameraSource mCameraSource = null;
    private View cursorView, camView;
    private CameraSourcePreview mPreview;
    private LayoutParams cursorLayout, camLayout;
    private WindowManager windowManager;
    private GraphicFaceTracker mFaceTracker;

    private static final int RC_HANDLE_GMS = 9001;

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();


    private static void logNodeHierachy(AccessibilityNodeInfo nodeInfo, int depth) {
        Rect bounds = new Rect();
        nodeInfo.getBoundsInScreen(bounds);

        StringBuilder sb = new StringBuilder();
        if (depth > 0) {
            for (int i=0; i<depth; i++) {
                sb.append("  ");
            }
            sb.append("\u2514 ");
        }
        sb.append(nodeInfo.getClassName());
        sb.append(" (" + nodeInfo.getChildCount() +  ")");
        sb.append(" " + bounds.toString());
        if (nodeInfo.getText() != null) {
            sb.append(" - \"" + nodeInfo.getText() + "\"");
        }
        Log.v(TAG, sb.toString());

        for (int i=0; i<nodeInfo.getChildCount(); i++) {
            AccessibilityNodeInfo childNode = nodeInfo.getChild(i);
            if (childNode != null) {
                logNodeHierachy(childNode, depth + 1);
            }
        }
    }

    private static AccessibilityNodeInfo findSmallestNodeAtPoint(AccessibilityNodeInfo sourceNode, int x, int y) {
        Rect bounds = new Rect();
        sourceNode.getBoundsInScreen(bounds);

        if (!bounds.contains(x, y)) {
            return null;
        }

        for (int i=0; i<sourceNode.getChildCount(); i++) {
            AccessibilityNodeInfo nearestSmaller = findSmallestNodeAtPoint(sourceNode.getChild(i), x, y);
            if (nearestSmaller != null) {
                return nearestSmaller;
            }
        }
        return sourceNode;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        cursorView = View.inflate(getBaseContext(), R.layout.cursor, null);
        cursorLayout = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                LayoutParams.TYPE_APPLICATION_OVERLAY,
                LayoutParams.FLAG_DISMISS_KEYGUARD | LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);
        cursorLayout.gravity = Gravity.TOP | Gravity.LEFT;

        camView = View.inflate(getBaseContext(), R.layout.preview, null);
        camLayout = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                LayoutParams.TYPE_APPLICATION_OVERLAY,
                LayoutParams.FLAG_DISMISS_KEYGUARD | LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);
        camLayout.gravity = Gravity.TOP | Gravity.LEFT;

        mPreview = (CameraSourcePreview) camView.findViewById(R.id.camView);//Getting camera preview from floating layout

        int mWidth= this.getResources().getDisplayMetrics().widthPixels;
        int mHeight= this.getResources().getDisplayMetrics().heightPixels;

        cursorLayout.x = mWidth/2;
        cursorLayout.y = mHeight/2;

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);


        createCameraSource();

    }

    private void createCameraSource() {

        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
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
        //mFaceTracker.setSensitivity(7);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (windowManager != null && cursorView != null) {
            windowManager.removeView(cursorView);
        }
        if (windowManager != null && camView != null) {
            windowManager.removeView(camView);
        }

        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    /**
    Click function
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void click() {
//        Log.d(TAG, String.format("Click [%d, %d]", cursorLayout.x, cursorLayout.y));
//        AccessibilityNodeInfo nodeInfo = this.getRootInActiveWindow();
//        if (nodeInfo == null) return;
//        AccessibilityNodeInfo nearestNodeToMouse = findSmallestNodeAtPoint(nodeInfo, cursorLayout.x, cursorLayout.y + 50);
//        if (nearestNodeToMouse != null) {
//            logNodeHierachy(nearestNodeToMouse, 0);
//            nearestNodeToMouse.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//        }
//        nodeInfo.recycle();
    }

    public void onMouseMove(int x, int y, boolean click) {

        if (click) {
            click();
        }

        cursorView.setVisibility(View.VISIBLE);

        cursorLayout.x = x;
        cursorLayout.y = y;

        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                windowManager.updateViewLayout(cursorView, cursorLayout);
            }
        });

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        windowManager.addView(cursorView, cursorLayout);
        windowManager.addView(camView, camLayout);

        return START_STICKY;
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        CursorAccessibilityService getService() {
            // Return this instance of LocalService so clients can call public methods
            return CursorAccessibilityService.this;
        }
    }

    //==============================================================================================
    // Camera Source Preview
    //==============================================================================================
    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face> {
        private String TAG = "GraphicFaceTracker";

        private volatile Face mFace;
        private float cx2, cy2, newX, newY;
        private float sensitivity = 7;

        GraphicFaceTracker() {
        }

        /*
      Returns coordinates of face.
       */
        public void getFaceCoord() {
            Face face = mFace;
            if (face != null) {

                for (Landmark landmark : face.getLandmarks()) {
                    if ((landmark.getType() == Landmark.NOSE_BASE)) {

                        WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                        Display display = window.getDefaultDisplay();
                        int width = display.getWidth();

                        float cx = width - landmark.getPosition().x;
                        float cy = landmark.getPosition().y;

                        if (cx2 == 0 && cy2 == 0) {
                            cx2 = cx;
                            cy2 = cy;

                        }
                        float deltaX = cx - cx2;
                        float deltaY = cy - cy2;

                        if (newX != 0) {
                            newX = newX + deltaX * sensitivity;
                            newY = newY + deltaY * sensitivity;
                        } else {
                            newX = cx;
                            newY = cy;
                        }

                        cx2 = cx;
                        cy2 = cy;

                        Log.e(TAG, newX + ", " + newY);
                    }

                }
            }
        }

        public void setSensitivity(float sensitivity) {
            this.sensitivity = sensitivity;
        }


        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mFace = face;
            getFaceCoord();
            onMouseMove((int)newX, (int)newY, false);
        }


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

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    public class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            mFaceTracker = new GraphicFaceTracker();
            return mFaceTracker;
        }
    }

}
