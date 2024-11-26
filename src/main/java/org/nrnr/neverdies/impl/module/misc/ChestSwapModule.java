package org.nrnr.neverdies.impl.module.misc;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.EnumConfig;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.init.Managers;

/**
 * @author Neverdies
 * @since 1.0
 */
public class ChestSwapModule extends ToggleModule {

    Config<Priority> priorityConfig = new EnumConfig<>("Priority", "The chestplate material to prioritize", Priority.NETHERITE, Priority.values());

    public ChestSwapModule() {
        super("ChestSwap", "Automatically swaps chestplate", ModuleCategory.MISCELLANEOUS);
    }

    @Override
    public void onEnable() {
        ItemStack armorStack = mc.player.getInventory().getArmorStack(2);
        if (armorStack.getItem() instanceof ArmorItem armorItem
                && armorItem.getSlotType() == EquipmentSlot.CHEST) {
            int elytraSlot = getElytraSlot();
            if (elytraSlot != -1) {
                Managers.INVENTORY.pickupSlot(elytraSlot);
                Managers.INVENTORY.pickupSlot(6);
                Managers.INVENTORY.pickupSlot(elytraSlot);
            }
        } else {
            int chestplateSlot = getChestplateSlot();
            if (chestplateSlot != -1) {
                Managers.INVENTORY.pickupSlot(chestplateSlot);
                Managers.INVENTORY.pickupSlot(6);
                Managers.INVENTORY.pickupSlot(chestplateSlot);
            }
        }
        disable();
    }

    private int getChestplateSlot() {
        int slot = -1;
        for (int i = 0; i < 45; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof ArmorItem armorItem
                    && armorItem.getSlotType() == EquipmentSlot.CHEST) {
                if (armorItem.getMaterial() == ArmorMaterials.NETHERITE && priorityConfig.getValue() == Priority.NETHERITE) {
                    slot = i;
                    break;
                } else if (armorItem.getMaterial() == ArmorMaterials.DIAMOND && priorityConfig.getValue() == Priority.DIAMOND) {
                    slot = i;
                    break;
                } else {
                    slot = i;
                }
            }
        }
        return slot;
    }

    private int getElytraSlot() {
        int slot = -1;
        for (int i = 0; i < 45; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof ElytraItem) {
                slot = i;
                break;
            }
        }
        return slot;
    }

    private enum Priority {
        NETHERITE,
        DIAMOND
    }
}
