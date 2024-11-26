package org.nrnr.neverdies.impl.event.network;

import org.nrnr.neverdies.api.event.Cancelable;
import org.nrnr.neverdies.api.event.Event;

@Cancelable
public class ReachEvent extends Event {
    private float reach;

    public float getReach() {
        return reach;
    }

    public void setReach(float reach) {
        this.reach = reach;
    }
}
