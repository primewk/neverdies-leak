package org.nrnr.neverdies.impl.event.render;

import net.minecraft.client.util.math.MatrixStack;
import org.nrnr.neverdies.api.event.Event;

/**
 * @author chronos
 * @since 1.0
 */
public class RenderWorldEvent extends Event {
    //
    private final MatrixStack matrices;
    private final float tickDelta;

    /**
     * @param matrices
     */
    public RenderWorldEvent(MatrixStack matrices, float tickDelta) {
        this.matrices = matrices;
        this.tickDelta = tickDelta;
    }

    /**
     * @return
     */
    public MatrixStack getMatrices() {
        return matrices;
    }

    /**
     * @return
     */
    public float getTickDelta() {
        return tickDelta;
    }

    public static class Game extends RenderWorldEvent {

        /**
         * @param matrices
         * @param tickDelta
         */
        public Game(MatrixStack matrices, float tickDelta) {
            super(matrices, tickDelta);
        }
    }
}
