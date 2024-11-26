package org.nrnr.neverdies.impl.event.network;

import org.nrnr.neverdies.api.event.Cancelable;
import org.nrnr.neverdies.api.event.Event;

@Cancelable
public class MountJumpStrengthEvent extends Event {
    //
    private float jumpStrength;

    public float getJumpStrength() {
        return jumpStrength;
    }

    public void setJumpStrength(float jumpStrength) {
        this.jumpStrength = jumpStrength;
    }
}
