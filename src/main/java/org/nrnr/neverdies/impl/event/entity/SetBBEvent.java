package org.nrnr.neverdies.impl.event.entity;

import net.minecraft.util.math.Box;
import org.nrnr.neverdies.api.event.Event;

public class SetBBEvent extends Event {

    private final Box boundingBox;

    public SetBBEvent(Box boundingBox) {
        this.boundingBox = boundingBox;
    }

    public Box getBoundingBox() {
        return boundingBox;
    }
}
