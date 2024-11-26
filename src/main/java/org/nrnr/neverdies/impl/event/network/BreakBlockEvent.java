package org.nrnr.neverdies.impl.event.network;

import net.minecraft.util.math.BlockPos;
import org.nrnr.neverdies.api.event.Cancelable;
import org.nrnr.neverdies.api.event.Event;

@Cancelable
public class BreakBlockEvent extends Event {
    private final BlockPos pos;

    public BreakBlockEvent(BlockPos pos) {
        this.pos = pos;
    }

    public BlockPos getPos() {
        return pos;
    }
}
