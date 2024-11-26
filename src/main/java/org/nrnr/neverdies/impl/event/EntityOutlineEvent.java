package org.nrnr.neverdies.impl.event;

import net.minecraft.entity.Entity;
import org.nrnr.neverdies.api.event.Cancelable;
import org.nrnr.neverdies.api.event.Event;

@Cancelable
public class EntityOutlineEvent extends Event {
    private final Entity entity;

    public EntityOutlineEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }
}
