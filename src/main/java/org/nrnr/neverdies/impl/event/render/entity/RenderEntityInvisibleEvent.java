package org.nrnr.neverdies.impl.event.render.entity;

import net.minecraft.entity.LivingEntity;
import org.nrnr.neverdies.api.event.Cancelable;
import org.nrnr.neverdies.api.event.Event;

@Cancelable
public class RenderEntityInvisibleEvent extends Event {
    private final LivingEntity entity;

    public RenderEntityInvisibleEvent(LivingEntity entity) {
        this.entity = entity;
    }

    public LivingEntity getEntity() {
        return entity;
    }
}
