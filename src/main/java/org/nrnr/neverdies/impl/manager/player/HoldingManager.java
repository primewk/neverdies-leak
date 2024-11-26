package org.nrnr.neverdies.impl.manager.player;


import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import static org.nrnr.neverdies.util.Globals.mc;

/**
 * @author OLEPOSSU
 */

public class HoldingManager {

    public static int slot;
    public static long modifyStartTime = 0;



    public ItemStack getStack() {
        if (mc.player == null) {
            return null;
        }
        return mc.player.getInventory().getStack(slot);
    }

    public int getSlot() {
        return slot;
    }

    public boolean isHolding(Item... items) {
        ItemStack stack = getStack();
        if (stack == null) {
            return false;
        }
        for (Item item : items) {
            if (item.equals(stack.getItem())) {
                return true;
            }
        }
        return false;
    }

    public boolean isHolding(Item item) {
        ItemStack stack = getStack();
        if (stack == null) {
            return false;
        }
        return stack.getItem().equals(item);
    }
}