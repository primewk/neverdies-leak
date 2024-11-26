package org.nrnr.neverdies.impl.event;

import org.nrnr.neverdies.api.event.Cancelable;
import org.nrnr.neverdies.api.event.Event;

/**
 *
 */
@Cancelable
public class FramerateLimitEvent extends Event {
    private int framerateLimit;

    public int getFramerateLimit() {
        return framerateLimit;
    }

    public void setFramerateLimit(int framerateLimit) {
        this.framerateLimit = framerateLimit;
    }
}
