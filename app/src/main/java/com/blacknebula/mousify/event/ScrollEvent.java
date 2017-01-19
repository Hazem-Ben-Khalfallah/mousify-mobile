package com.blacknebula.mousify.event;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

/**
 * @author hazem
 */
@Parcel
public class ScrollEvent {
    private static int FACTOR = 10;
    int amount;

    public ScrollEvent() {
    }

    @ParcelConstructor
    public ScrollEvent(int amount) {
        this.amount = (amount / FACTOR);
    }

    public int getAmount() {
        return amount;
    }
}
