package org.nrnr.neverdies.mixin.chunk.light;

import net.minecraft.world.chunk.light.ChunkSkyLightProvider;
import org.nrnr.neverdies.Neverdies;
import org.nrnr.neverdies.impl.event.chunk.light.RenderSkylightEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author chronos
 * @see ChunkSkyLightProvider
 * @since 1.0
 */
@Mixin(ChunkSkyLightProvider.class)
public class MixinChunkSkylightProvider {
    /**
     * @param blockPos
     * @param l
     * @param lightLevel
     * @param ci
     */
    @Inject(method = "method_51531", at = @At(value = "HEAD"), cancellable = true)
    private void hookRecalculateLevel(long blockPos, long l, int lightLevel, CallbackInfo ci) {
        RenderSkylightEvent renderSkylightEvent = new RenderSkylightEvent();
        Neverdies.EVENT_HANDLER.dispatch(renderSkylightEvent);
        if (renderSkylightEvent.isCanceled()) {
            ci.cancel();
        }
    }
}
