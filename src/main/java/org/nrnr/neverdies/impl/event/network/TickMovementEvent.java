package org.nrnr.neverdies.impl.event.network;

import org.nrnr.neverdies.api.event.Cancelable;
import org.nrnr.neverdies.api.event.Event;

/**
 * @author chronos
 * @since 1.0
 */
@Cancelable
public class TickMovementEvent extends Event {
    //
    private int iterations;

    /**
     * @return
     */
    public int getIterations() {
        return iterations;
    }

    /**
     * @param iterations
     */
    public void setIterations(int iterations) {
        this.iterations = iterations;
    }
}
