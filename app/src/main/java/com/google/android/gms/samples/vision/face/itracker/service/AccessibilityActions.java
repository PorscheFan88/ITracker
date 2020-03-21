package com.google.android.gms.samples.vision.face.itracker.service;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * Class performs all accessibility actions (click, swipe, long-click, etc.)
 */
public class AccessibilityActions {


    private static final String TAG = CursorService.class.getName();
    private CursorService cursorService;
    private AccessibilityNodeInfo nodeInfo;

    AccessibilityActions(CursorService cs) {
        cursorService = cs;
    }

    /**
     Click function
     */
    public void click() {

        Log.d(TAG, String.format("Click [%d, %d]", cursorService.getX(), cursorService.getY()));
        nodeInfo = cursorService.getRootInActiveWindow();

        if (nodeInfo == null) return;
        AccessibilityNodeInfo nearestNodeToMouse = findSmallestNodeAtPoint(nodeInfo, cursorService.getX(), cursorService.getY()+50);
        if (nearestNodeToMouse != null) {
            logNodeHierachy(nearestNodeToMouse, 0);
            nearestNodeToMouse.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        } else {
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT);
            Log.e(TAG, "Nearest Node is null");
        }
    }

    /**
    Long Click function
     */
    public void longClick() {

        Log.d(TAG, String.format("Click [%d, %d]", cursorService.getX(), cursorService.getY()));
        nodeInfo = cursorService.getRootInActiveWindow();

        if (nodeInfo == null) return;
        AccessibilityNodeInfo nearestNodeToMouse = findSmallestNodeAtPoint(nodeInfo, cursorService.getX(), cursorService.getY()+50);
        if (nearestNodeToMouse != null) {
            logNodeHierachy(nearestNodeToMouse, 0);
            nearestNodeToMouse.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
        } else {
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT);
            Log.e(TAG, "Nearest Node is null");
        }
    }

    public void goHome(Context ctx) {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(startMain);
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

}
