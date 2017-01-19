package com.blacknebula.mousify.dto;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

/**
 * @author hazem
 */
@Parcel
public class MotionRequest {
    int x;
    int y;

    @ParcelConstructor
    public MotionRequest(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
