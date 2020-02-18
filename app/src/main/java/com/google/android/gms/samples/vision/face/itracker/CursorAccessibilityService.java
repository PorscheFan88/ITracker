package com.google.android.gms.samples.vision.face.itracker;

import android.accessibilityservice.AccessibilityService;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Rect;

import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;

import com.google.android.gms.samples.vision.face.itracker.ui.camera.CameraSourcePreview;
import com.google.android.gms.samples.vision.face.itracker.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class CursorAccessibilityService extends Service {
    private static final String TAG = CursorAccessibilityService.class.getName();
    private View cursorView;
    private LayoutParams cursorLayout;
    private WindowManager windowManager;
    private boolean tracking = false;


    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    public void setTracking(boolean tracking) {
        this.tracking = tracking;
    }

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
        int mWidth= this.getResources().getDisplayMetrics().widthPixels;
        int mHeight= this.getResources().getDisplayMetrics().heightPixels;

        cursorLayout.x = mWidth/2;
        cursorLayout.y = mHeight/2;

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        if (!tracking)
            cursorView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (windowManager != null && cursorView != null) {
            windowManager.removeView(cursorView);
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

        if (tracking) {
            if (click)
                click();

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

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        windowManager.addView(cursorView, cursorLayout);

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
    // Graphic Face Tracker
    //==============================================================================================




}
