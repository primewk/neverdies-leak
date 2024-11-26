package org.nrnr.neverdies.impl.event.world;

import net.minecraft.util.math.ChunkPos;
import org.nrnr.neverdies.api.event.StageEvent;

/**
 * @author chronos
 * @since 1.0
 */
public class ChunkLoadEvent extends StageEvent {
    // Chunk position. Needs scaling
    private final ChunkPos pos;

    /**
     * @param pos
     */
    public ChunkLoadEvent(ChunkPos pos) {
        this.pos = pos;
    }

    /**
     * @return
     */
    public ChunkPos getPos() {
        return pos;
    }
}
