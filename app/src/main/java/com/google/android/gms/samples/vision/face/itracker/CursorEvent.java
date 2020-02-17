package com.google.android.gms.samples.vision.face.itracker;

public class CursorEvent {

    public static final int
            MOVE_UP = 0,
            MOVE_DOWN = 1,
            MOVE_LEFT = 2,
            MOVE_RIGHT = 3,
            LEFT_CLICK = 4;

    public final int direction;

    public CursorEvent(int direction) {
        this.direction = direction;
    }

}
