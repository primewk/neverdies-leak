package org.nrnr.neverdies.impl.event.network;

import org.nrnr.neverdies.api.event.StageEvent;

public class PlayerUpdateEvent extends StageEvent {
    private float yaw;
    private float pitch;

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }
}
