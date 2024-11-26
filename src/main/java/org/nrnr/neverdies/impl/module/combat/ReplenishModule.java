package org.nrnr.neverdies.impl.module.combat;

import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.NumberConfig;
import org.nrnr.neverdies.api.event.EventStage;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.impl.event.TickEvent;
import org.nrnr.neverdies.init.Managers;

import java.util.ArrayList;

/**
 * @author chronos
 * @since 1.0
 */
public class ReplenishModule extends ToggleModule {

    Config<Integer> percentConfig = new NumberConfig<>("Percent", "The minimum percent of total stack before replenishing", 0, 25, 80);

    public ReplenishModule() {
        super("Replenish", "Automatically replaces items in your hotbar", ModuleCategory.COMBAT);
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() != EventStage.PRE) {
            return;
        }
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || !stack.isStackable()) {
                continue;
            }
            double stackPercent = ((float) stack.getCount() / stack.getMaxCount()) * 100.0f;
            if (stack.getCount() == 1 || stackPercent <= Math.max(percentConfig.getValue(), 5.0f)) {
                replenishStack(stack, i);
            }
        }
    }

    private void replenishStack(ItemStack item, int hotbarSlot) {
        int total = item.getCount();
        for (int i = 9; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            // We cannot merge stacks if they don't have the same name
            if (!stack.getName().equals(item.getName())) {
                continue;
            }
            if (stack.getItem() instanceof BlockItem blockItem && (!(item.getItem() instanceof BlockItem blockItem1) || blockItem.getBlock() != blockItem1.getBlock())) {
                continue;
            }
            if (stack.getItem() != item.getItem()) {
                continue;
            }
            if (total < stack.getMaxCount()) {
                Managers.INVENTORY.pickupSlot(i);
                Managers.INVENTORY.pickupSlot(hotbarSlot + 36);
                if (!mc.player.playerScreenHandler.getCursorStack().isEmpty()) {
                    Managers.INVENTORY.pickupSlot(i);
                }
                total += stack.getCount();
            }
        }
    }
}
