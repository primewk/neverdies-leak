package org.nrnr.neverdies.impl.event.biome;

import net.minecraft.world.biome.BiomeParticleConfig;
import org.nrnr.neverdies.api.event.Cancelable;
import org.nrnr.neverdies.api.event.Event;

@Cancelable
public class BiomeEffectsEvent extends Event {

    private BiomeParticleConfig particleConfig;

    public BiomeParticleConfig getParticleConfig() {
        return particleConfig;
    }

    public void setParticleConfig(BiomeParticleConfig particleConfig) {
        this.particleConfig = particleConfig;
    }
}
