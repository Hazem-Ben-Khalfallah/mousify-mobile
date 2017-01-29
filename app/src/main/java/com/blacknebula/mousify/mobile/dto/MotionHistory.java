package com.blacknebula.mousify.mobile.dto;


/**
 * @author hazem
 */

public class MotionHistory {
    private static final int Y_THRESHOLD = 10;
    private static final int X_THRESHOLD = 10;
    private static final int SCROLL_THRESHOLD = 0;

    private static MotionHistory motionHistory;
    private int startX;
    private int startY;
    private int currentY;
    private int currentX;
    private int downX;
    private int downY;
    private int upX;
    private int upY;

    private MotionHistory() {
    }

    public static MotionHistory getInstance() {
        if (motionHistory == null) {
            motionHistory = new MotionHistory();
        }
        return motionHistory;
    }

    public static boolean shouldIgnoreMove(int dx, int dy) {
        return Math.abs(dx) < X_THRESHOLD && Math.abs(dy) < Y_THRESHOLD;
    }

    public static boolean shouldIgnoreScroll(int amount) {
        return Math.abs(amount) == SCROLL_THRESHOLD;
    }

    public void updateDownCoordinates(float x, float y) {
        this.downX = Math.round(x);
        this.downY = Math.round(y);
        updateStartCoordinates(x, y);
    }

    public void updateUpCoordinates(float x, float y) {
        this.upX = Math.round(x);
        this.upY = Math.round(y);
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

    public int getDownX() {
        return downX;
    }

    public int getDownY() {
        return downY;
    }

    public int getUpX() {
        return upX;
    }

    public int getUpY() {
        return upY;
    }

    public boolean isClickEvent() {
        return shouldIgnoreMove(downX - upX, downY - upY);
    }
}
