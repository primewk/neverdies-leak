package org.nrnr.neverdies.mixin.accessor;

import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author chronos
 * @see ClientPlayerEntity
 * @since 1.0
 */
@Mixin(ClientPlayerEntity.class)
public interface AccessorClientPlayerEntity {
    /**
     * @return
     */
    @Accessor("lastX")
    double getLastX();

    /**
     * @return
     */
    @Accessor("lastBaseY")
    double getLastBaseY();

    /**
     * @return
     */
    @Accessor("lastZ")
    double getLastZ();
}
