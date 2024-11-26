package org.nrnr.neverdies.mixin.biome;

import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.biome.BiomeParticleConfig;
import org.nrnr.neverdies.Neverdies;
import org.nrnr.neverdies.impl.event.biome.BiomeEffectsEvent;
import org.nrnr.neverdies.impl.event.world.SkyboxEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(BiomeEffects.class)
public class MixinBiomeEffects {

    /**
     * @param cir
     */
    @Inject(method = "getSkyColor", at = @At(value = "HEAD"), cancellable = true)
    private void hookGetSkyColor(CallbackInfoReturnable<Integer> cir) {
        SkyboxEvent.Sky skyboxEvent = new SkyboxEvent.Sky();
        Neverdies.EVENT_HANDLER.dispatch(skyboxEvent);
        if (skyboxEvent.isCanceled()) {
            cir.cancel();
            cir.setReturnValue(skyboxEvent.getRGB());
        }
    }

    /**
     * @param cir
     */
    @Inject(method = "getParticleConfig", at = @At(value = "HEAD"), cancellable = true)
    private void hookGetParticleConfig(CallbackInfoReturnable<Optional<BiomeParticleConfig>> cir) {
        BiomeEffectsEvent biomeEffectsEvent = new BiomeEffectsEvent();
        Neverdies.EVENT_HANDLER.dispatch(biomeEffectsEvent);
        if (biomeEffectsEvent.isCanceled()) {
            cir.cancel();
            cir.setReturnValue(Optional.ofNullable(biomeEffectsEvent.getParticleConfig()));
        }
    }
}
