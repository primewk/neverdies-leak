package org.nrnr.neverdies.impl.event.gui;

import net.minecraft.client.util.math.MatrixStack;
import org.nrnr.neverdies.api.event.Event;

public class RenderScreenEvent extends Event {
    public final MatrixStack matrixStack;

    public RenderScreenEvent(MatrixStack matrixStack) {
        this.matrixStack = matrixStack;
    }
}
