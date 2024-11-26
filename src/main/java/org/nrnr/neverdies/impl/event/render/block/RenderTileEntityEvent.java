package org.nrnr.neverdies.impl.event.render.block;

import org.nrnr.neverdies.api.event.Cancelable;
import org.nrnr.neverdies.api.event.Event;

/**
 * @author chronos
 * @since 1.0
 */
public class RenderTileEntityEvent extends Event {
    @Cancelable
    public static class EnchantingTableBook extends RenderTileEntityEvent {

    }
}
