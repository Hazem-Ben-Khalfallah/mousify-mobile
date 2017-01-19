package com.blacknebula.mousify.dto;


/**
 * @author hazem
 */

public class MotionHistory {
    private static final int Y_THRESHOLD = 20;
    private static final int X_THRESHOLD = 20;

    private static MotionHistory motionHistory;
    private int startX;
    private int startY;
    private int currentY;
    private int currentX;

    private MotionHistory() {
    }

    public static MotionHistory getInstance() {
        if (motionHistory == null) {
            motionHistory = new MotionHistory();
        }
        return motionHistory;
    }

    public void updateStartCoordinates(float x, float y) {
        this.startX = Math.round(x);
        this.startY = Math.round(y);
    }

    public void updateCurrentCoordinates(float x, float y) {
        this.currentX = Math.round(x);
        this.currentY = Math.round(y);
    }

    public void resetStartToCurrentPosition() {
        this.startX = this.currentX;
        this.startY = this.currentY;
    }

    public static boolean shouldIgnoreMove(int dx, int dy) {
        return Math.abs(dx) < X_THRESHOLD && Math.abs(dy) < Y_THRESHOLD;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    public int getCurrentY() {
        return currentY;
    }

    public int getCurrentX() {
        return currentX;
    }
}
