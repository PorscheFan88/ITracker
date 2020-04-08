package com.google.android.gms.samples.vision.face.itracker.service;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
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
            nearestNodeToMouse.performAction(AccessibilityNodeInfo.ACTION_SELECT);
            nearestNodeToMouse.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        } else {
            Log.e(TAG, "Nearest Node is null");
        }
    }

    /**
    Long Click function
     */
    public void longClick() {

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

    private  AccessibilityNodeInfo findSmallestNodeAtPoint(AccessibilityNodeInfo sourceNode, int x, int y) {
        Rect bounds = new Rect();
        sourceNode.getBoundsInScreen(bounds);

        if (!bounds.contains(x, y)) {
            return null;
        }


        for (int i = 0; i < sourceNode.getChildCount(); i++) {
            AccessibilityNodeInfo nearestSmaller = findSmallestNodeAtPoint(sourceNode.getChild(i), x, y);
            if (nearestSmaller != null) {
                if (nearestSmaller.getActionList().contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK)) {

                    return nearestSmaller;
                } else {
                    return sourceNode;
                }
            }
        }
        return sourceNode;
    }

}
