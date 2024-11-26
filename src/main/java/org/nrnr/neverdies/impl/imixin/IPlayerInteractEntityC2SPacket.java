package org.nrnr.neverdies.impl.imixin;

import net.minecraft.entity.Entity;
import org.nrnr.neverdies.util.network.InteractType;

/**
 *
 */
public interface IPlayerInteractEntityC2SPacket {
    /**
     * @return
     */
    Entity getEntity();

    /**
     * @return
     */
    InteractType getType();
}
