package org.nrnr.neverdies.impl.event.entity;

import net.minecraft.entity.LivingEntity;
import org.nrnr.neverdies.api.event.Event;

public class EntityDeathEvent extends Event {

    private final LivingEntity entity;

    public EntityDeathEvent(LivingEntity entity) {
        this.entity = entity;
    }

    public LivingEntity getEntity() {
        return entity;
    }
}
