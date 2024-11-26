package org.nrnr.neverdies.mixin.render;

import net.minecraft.client.render.RenderTickCounter;
import org.nrnr.neverdies.Neverdies;
import org.nrnr.neverdies.impl.event.render.TickCounterEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author chronos
 * @see RenderTickCounter
 * @since 1.0
 */
@Mixin(RenderTickCounter.class)
public class MixinRenderTickCounter {
    @Shadow
    private float lastFrameDuration;
    @Shadow
    private float tickDelta;
    @Shadow
    private long prevTimeMillis;
    @Shadow
    private float tickTime;

    /**
     * @param timeMillis
     * @param cir
     */
    @Inject(method = "beginRenderTick", at = @At(value = "HEAD"),
            cancellable = true)
    private void hookBeginRenderTick(long timeMillis,
                                     CallbackInfoReturnable<Integer> cir) {
        TickCounterEvent tickCounterEvent = new TickCounterEvent();
        Neverdies.EVENT_HANDLER.dispatch(tickCounterEvent);
        if (tickCounterEvent.isCanceled()) {
            lastFrameDuration = ((timeMillis - prevTimeMillis) / tickTime) * tickCounterEvent.getTicks();
            prevTimeMillis = timeMillis;
            tickDelta += lastFrameDuration;
            int i = (int) tickDelta;
            tickDelta -= i;
            cir.setReturnValue(i);
        }
    }
}
