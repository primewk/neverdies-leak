package org.nrnr.neverdies.mixin.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.SlimeBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.nrnr.neverdies.Neverdies;
import org.nrnr.neverdies.impl.event.block.SteppedOnSlimeBlockEvent;
import org.nrnr.neverdies.util.Globals;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author chronos
 * @see SlimeBlock
 * @since 1.0
 */
@Mixin(SlimeBlock.class)
public class MixinSlimeBlock implements Globals {
    /**
     * @param world
     * @param pos
     * @param state
     * @param entity
     * @param ci
     */
    @Inject(method = "onSteppedOn", at = @At(value = "HEAD"),
            cancellable = true)
    private void hookOnSteppedOn(World world, BlockPos pos, BlockState state,
                                 Entity entity, CallbackInfo ci) {
        SteppedOnSlimeBlockEvent steppedOnSlimeBlockEvent =
                new SteppedOnSlimeBlockEvent();
        Neverdies.EVENT_HANDLER.dispatch(steppedOnSlimeBlockEvent);
        if (steppedOnSlimeBlockEvent.isCanceled() && entity == mc.player) {
            ci.cancel();
        }
    }
}
