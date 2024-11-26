package org.nrnr.neverdies.impl.gui.click.component;

import net.minecraft.client.gui.DrawContext;

/**
 * @author chronos
 * @since 1.0
 */
public interface Drawable {
    /**
     * @param context
     * @param mouseX
     * @param mouseY
     * @param delta
     */
    void render(DrawContext context, float mouseX, float mouseY, float delta);
}
