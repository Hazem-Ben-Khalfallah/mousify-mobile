package com.blacknebula.mousify.event;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

/**
 * @author hazem
 */
@Parcel
public class ClickEvent {
    public final static String ACTION_CLICK = "click";
    public final static String ACTION_DOWN = "down";
    public final static String ACTION_UP = "up";
    boolean isLeft;
    String action;

    public ClickEvent() {
    }

    @ParcelConstructor
    public ClickEvent(boolean isLeft, String action) {
        this.isLeft = isLeft;
        this.action = action;
    }

}
