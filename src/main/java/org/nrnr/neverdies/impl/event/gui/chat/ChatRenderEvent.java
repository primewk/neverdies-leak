package org.nrnr.neverdies.impl.event.gui.chat;

import net.minecraft.client.gui.DrawContext;
import org.nrnr.neverdies.api.event.Cancelable;
import org.nrnr.neverdies.api.event.Event;

/**
 * @author chronos
 * @since 1.0
 */
@Cancelable
public class ChatRenderEvent extends Event {
    //
    private final DrawContext context;
    private final float x, y;

    public ChatRenderEvent(DrawContext context, float x, float y) {
        this.context = context;
        this.x = x;
        this.y = y;
    }

    public DrawContext getContext() {
        return context;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
