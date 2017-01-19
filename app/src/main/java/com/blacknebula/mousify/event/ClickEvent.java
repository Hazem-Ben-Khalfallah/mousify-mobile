package com.blacknebula.mousify.event;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

/**
 * @author hazem
 */
@Parcel
public class ClickEvent {
    boolean isLeft;

    public ClickEvent() {
    }

    @ParcelConstructor
    public ClickEvent(boolean isLeft) {
        this.isLeft = isLeft;
    }

}
