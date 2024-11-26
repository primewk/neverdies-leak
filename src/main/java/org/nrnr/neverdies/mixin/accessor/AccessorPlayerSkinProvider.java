package org.nrnr.neverdies.mixin.accessor;

import net.minecraft.client.texture.PlayerSkinProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author chronos
 * @since 1.0
 */
@Mixin(PlayerSkinProvider.class)
public interface AccessorPlayerSkinProvider {
    /**
     * @return
     */
    @Accessor("skinCache")
    PlayerSkinProvider.FileCache getSkinCacheDir();
}
