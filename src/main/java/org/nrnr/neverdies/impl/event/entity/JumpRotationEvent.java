package org.nrnr.neverdies.impl.event.entity;

import org.nrnr.neverdies.api.event.Event;

public final class JumpRotationEvent extends Event {
    private float yaw;


    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }
}
