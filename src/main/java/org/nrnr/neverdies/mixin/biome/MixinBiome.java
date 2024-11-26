package org.nrnr.neverdies.mixin.biome;

import net.minecraft.world.biome.Biome;
import org.nrnr.neverdies.Neverdies;
import org.nrnr.neverdies.impl.event.world.SkyboxEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Biome.class)
public class MixinBiome {

    @Inject(method = "getFogColor", at = @At(value = "HEAD"), cancellable = true)
    private void hookGetFogColor(CallbackInfoReturnable<Integer> cir) {
        SkyboxEvent.Fog skyboxEvent = new SkyboxEvent.Fog(0.0f);
        Neverdies.EVENT_HANDLER.dispatch(skyboxEvent);
        if (skyboxEvent.isCanceled()) {
            cir.cancel();
            cir.setReturnValue(skyboxEvent.getRGB());
        }
    }
}
