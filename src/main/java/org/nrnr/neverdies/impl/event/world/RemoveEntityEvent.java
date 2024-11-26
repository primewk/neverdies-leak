package org.nrnr.neverdies.impl.event.world;

import net.minecraft.entity.Entity;
import org.nrnr.neverdies.api.event.Event;
import org.nrnr.neverdies.util.Globals;

public class RemoveEntityEvent extends Event implements Globals {
    private final Entity entity;
    private final Entity.RemovalReason removalReason;

    public RemoveEntityEvent(Entity entity, Entity.RemovalReason removalReason) {
        this.entity = entity;
        this.removalReason = removalReason;
    }

    /**
     * @return
     */
    public Entity getEntity() {
        return entity;
    }

    public Entity.RemovalReason getRemovalReason() {
        return removalReason;
    }
}
