package org.nrnr.neverdies.api.module;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.impl.module.combat.SurroundModule;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author chronos
 * @see SurroundModule
 * @since 1.0
 */
public class ObsidianPlacerModule extends BlockPlacerModule {
    private static final List<Block> RESISTANT_BLOCKS = new LinkedList<>() {{
        add(Blocks.OBSIDIAN);
        add(Blocks.CRYING_OBSIDIAN);
        add(Blocks.ENDER_CHEST);
    }};

    public ObsidianPlacerModule(String name, String desc, ModuleCategory category) {
        super(name, desc, category);
    }

    public ObsidianPlacerModule(String name, String desc, ModuleCategory category, int rotationPriority) {
        super(name, desc, category, rotationPriority);
    }

    /**
     * @return
     */
    protected int getResistantBlockItem() {
        for (final Block type : RESISTANT_BLOCKS) {
            final int slot = getBlockItemSlot(type);
            if (slot != -1) {
                return slot;
            }
        }
        return -1;
    }
}
