package org.nrnr.neverdies.impl.event.render;

import org.nrnr.neverdies.api.event.Cancelable;
import org.nrnr.neverdies.api.event.Event;

/**
 * @author chronos
 * @since 1.0
 */
@Cancelable
public class TickCounterEvent extends Event {
    //
    private float ticks;

    /**
     * @return
     */
    public float getTicks() {
        return ticks;
    }

    /**
     * @param ticks
     */
    public void setTicks(float ticks) {
        this.ticks = ticks;
    }
}
