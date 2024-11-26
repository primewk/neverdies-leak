package org.nrnr.neverdies.impl.module.world;

import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.NumberDisplay;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.config.setting.NumberConfig;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.impl.event.network.PlayerTickEvent;

/**
 * @author xgraza
 * @since 04/13/24
 */
public final class BlockInteractModule extends ToggleModule {
    Config<Float> range = new NumberConfig<>("Range", "", 1.0f, 4.0f, 10.0f, NumberDisplay.DEFAULT);
    Config<Boolean> fluids = new BooleanConfig("Fluids", "", false);

    public BlockInteractModule() {
        super("BlockInteract", "Allows you to place blocks in the air", ModuleCategory.WORLD);
    }

    @EventListener
    public void onPlayerTick(final PlayerTickEvent event) {
        final ItemStack stack = mc.player.getMainHandStack();
        if ((stack.isEmpty() || !(stack.getItem() instanceof BlockItem)) || !mc.options.useKey.isPressed()) {
            return;
        }

        final HitResult result = mc.player.raycast(range.getValue(), 1.0f, fluids.getValue());
        if (result instanceof BlockHitResult blockHitResult) {
            final ActionResult actionResult = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, blockHitResult);
            if (actionResult.isAccepted() && actionResult.shouldSwingHand()) {
                mc.player.swingHand(Hand.MAIN_HAND);
            }
        }
    }
}
