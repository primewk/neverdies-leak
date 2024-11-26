package org.nrnr.neverdies.impl.event.entity.player;

import net.minecraft.entity.Entity;
import org.nrnr.neverdies.api.event.Cancelable;
import org.nrnr.neverdies.api.event.Event;

/**
 * @author chronos
 * @since 1.0
 */
@Cancelable
public class PushEntityEvent extends Event {
    private final Entity pushed, pusher;

    public PushEntityEvent(Entity pushed, Entity pusher) {
        this.pushed = pushed;
        this.pusher = pusher;
    }

    public Entity getPushed() {
        return pushed;
    }

    public Entity getPusher() {
        return pusher;
    }
}
