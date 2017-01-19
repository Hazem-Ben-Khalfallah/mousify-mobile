package com.blacknebula.mousify.dto;


/**
 * @author hazem
 */

public class MotionHistory {
    private static final int Y_THRESHOLD = 20;
    private static final int X_THRESHOLD = 20;

    public static MotionHistory motionHistory;
    private int previousX;
    private int previousY;

    private MotionHistory() {
    }

    public static MotionHistory getInstance() {
        if (motionHistory == null) {
            motionHistory = new MotionHistory();
        }
        return motionHistory;
    }

    public void updateHistory(int x, int y) {
        this.previousX = x;
        this.previousY = y;
    }

    public boolean shouldIgnoreMove(MotionRequest motionRequest) {
        return Math.abs(motionRequest.getX() - previousX) < X_THRESHOLD ||
                Math.abs(motionRequest.getY() - previousY) < Y_THRESHOLD;
    }
}
