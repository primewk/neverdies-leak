package org.nrnr.neverdies.impl.event;

import org.nrnr.neverdies.api.event.Cancelable;
import org.nrnr.neverdies.api.event.Event;

@Cancelable
public class MouseUpdateEvent extends Event {

    private final double cursorDeltaX;
    private final double cursorDeltaY;

    public MouseUpdateEvent(double cursorDeltaX, double cursorDeltaY) {
        this.cursorDeltaX = cursorDeltaX;
        this.cursorDeltaY = cursorDeltaY;
    }

    public double getCursorDeltaX() {
        return cursorDeltaX;
    }

    public double getCursorDeltaY() {
        return cursorDeltaY;
    }
}

