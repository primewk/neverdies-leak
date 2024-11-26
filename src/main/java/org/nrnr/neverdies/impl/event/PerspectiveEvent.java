package org.nrnr.neverdies.impl.event;

import net.minecraft.client.render.Camera;
import org.nrnr.neverdies.api.event.Cancelable;
import org.nrnr.neverdies.api.event.Event;

@Cancelable
public class PerspectiveEvent extends Event {

    public Camera camera;

    public PerspectiveEvent(Camera camera) {
        this.camera = camera;
    }

    public Camera getCamera() {
        return camera;
    }

}
