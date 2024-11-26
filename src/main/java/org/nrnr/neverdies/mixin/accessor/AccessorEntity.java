package org.nrnr.neverdies.mixin.accessor;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * @author chronos
 * @see Entity
 * @since 1.0
 */
@Mixin(Entity.class)
public interface AccessorEntity {
    /**
     *
     */
    @Invoker("unsetRemoved")
    void hookUnsetRemoved();

    /**
     * @param index
     * @param value
     */
    @Invoker("setFlag")
    void hookSetFlag(int index, boolean value);
}
