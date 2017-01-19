package com.blacknebula.mousify.event;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

/**
 * @author hazem
 */
@Parcel
public class ClickEvent {
    boolean isDoubleClick;

    public ClickEvent() {
    }

    @ParcelConstructor
    public ClickEvent(boolean isDoubleClick) {
        this.isDoubleClick = isDoubleClick;
    }

}
