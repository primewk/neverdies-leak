package org.nrnr.neverdies.impl.event.render;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.nrnr.neverdies.api.event.Cancelable;
import org.nrnr.neverdies.api.event.Event;

/**
 * @author chronos
 * @since 1.0
 */
@Cancelable
public class RenderFloatingItemEvent extends Event {
    private final ItemStack floatingItem;

    public RenderFloatingItemEvent(ItemStack floatingItem) {
        this.floatingItem = floatingItem;
    }

    public Item getFloatingItem() {
        return floatingItem.getItem();
    }

    public ItemStack getFloatingItemStack() {
        return floatingItem;
    }
}
