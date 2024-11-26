package org.nrnr.neverdies.impl.event.particle;

import org.nrnr.neverdies.api.event.Cancelable;
import org.nrnr.neverdies.api.event.Event;

import java.awt.*;

@Cancelable
public class TotemParticleEvent extends Event {
    private Color color;

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
