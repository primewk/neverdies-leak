package org.nrnr.neverdies.impl.event.render.entity;

import net.minecraft.entity.Entity;
import org.nrnr.neverdies.api.event.Cancelable;
import org.nrnr.neverdies.api.event.Event;

/**
 * @author chronos
 * @since 1.0
 */
@Cancelable
public class RenderLabelEvent extends Event {
    private final Entity entity;

    public RenderLabelEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }
}
