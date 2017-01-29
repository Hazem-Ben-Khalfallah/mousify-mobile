package com.blacknebula.mousify.mobile.event;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

/**
 * @author hazem
 */
@Parcel
public class MotionEvent {
    int dx;
    int dy;

    public MotionEvent() {
    }

    @ParcelConstructor
    public MotionEvent(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public static MotionEvent builder() {
        return new MotionEvent();
    }

    public MotionEvent withDx(float dx) {
        this.dx = Math.round(dx);
        return this;
    }

    public MotionEvent withDy(float dy) {
        this.dy = Math.round(dy);
        return this;
    }


    public int getDx() {
        return dx;
    }

    public void setDx(int dx) {
        this.dx = dx;
    }

    public int getDy() {
        return dy;
    }

    public void setDy(int dy) {
        this.dy = dy;
    }
}
