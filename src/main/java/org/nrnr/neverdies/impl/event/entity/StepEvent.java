package org.nrnr.neverdies.impl.event.entity;

import org.nrnr.neverdies.api.event.Event;

public class StepEvent extends Event {
    private final double stepHeight;

    public StepEvent(double stepHeight) {
        this.stepHeight = stepHeight;
    }

    public double getStepHeight() {
        return stepHeight;
    }
}
