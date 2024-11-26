package org.nrnr.neverdies.impl.event.render;

import org.nrnr.neverdies.api.event.Cancelable;
import org.nrnr.neverdies.api.event.Event;
import org.nrnr.neverdies.mixin.render.MixinLightmapTextureManager;

/**
 * @author chronos
 * @see MixinLightmapTextureManager
 * @since 1.0
 */
@Cancelable
public class LightmapGammaEvent extends Event {
    //
    private int gamma;

    /**
     * @param gamma
     */
    public LightmapGammaEvent(int gamma) {
        this.gamma = gamma;
    }

    public int getGamma() {
        return gamma;
    }

    public void setGamma(int gamma) {
        this.gamma = gamma;
    }
}
