package org.nrnr.neverdies.impl.event.entity;

import net.minecraft.block.BlockState;
import org.nrnr.neverdies.api.event.Cancelable;
import org.nrnr.neverdies.api.event.Event;

@Cancelable
public class SlowMovementEvent extends Event {
    private final BlockState state;

    public SlowMovementEvent(BlockState state) {
        this.state = state;
    }

    public BlockState getState() {
        return state;
    }
}
