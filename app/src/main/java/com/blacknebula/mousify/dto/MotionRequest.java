package com.blacknebula.mousify.dto;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

/**
 * @author hazem
 */
@Parcel
public class MotionRequest {
    int dx;
    int dy;

    public MotionRequest() {
    }

    @ParcelConstructor
    public MotionRequest(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public static MotionRequest builder() {
        return new MotionRequest();
    }

    public MotionRequest withDx(float dx) {
        this.dx = Math.round(dx);
        return this;
    }

    public MotionRequest withDy(float dy) {
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
