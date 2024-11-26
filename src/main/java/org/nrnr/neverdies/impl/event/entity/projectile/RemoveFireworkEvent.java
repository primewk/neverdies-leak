package org.nrnr.neverdies.impl.event.entity.projectile;

import net.minecraft.entity.projectile.FireworkRocketEntity;
import org.nrnr.neverdies.api.event.Cancelable;
import org.nrnr.neverdies.api.event.Event;

@Cancelable
public class RemoveFireworkEvent extends Event {
    private final FireworkRocketEntity rocketEntity;

    public RemoveFireworkEvent(FireworkRocketEntity rocketEntity) {
        this.rocketEntity = rocketEntity;
    }

    public FireworkRocketEntity getRocketEntity() {
        return rocketEntity;
    }
}
