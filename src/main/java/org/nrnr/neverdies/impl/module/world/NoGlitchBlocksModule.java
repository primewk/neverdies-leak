package org.nrnr.neverdies.impl.module.world;

import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.impl.event.network.BreakBlockEvent;
import org.nrnr.neverdies.impl.event.network.InteractBlockEvent;
import org.nrnr.neverdies.init.Managers;

/**
 * @author chronos
 * @since 1.0
 */
public class NoGlitchBlocksModule extends ToggleModule {

    Config<Boolean> placeConfig = new BooleanConfig("Place", "Places blocks only after the server confirms", true);
    Config<Boolean> destroyConfig = new BooleanConfig("Destroy", "Destroys blocks only after the server confirms", true);

    public NoGlitchBlocksModule() {
        super("NoGlitchBlocks", "Prevents blocks from being glitched in the world",
                ModuleCategory.WORLD);
    }

    /**
     * @param event
     */
    @EventListener
    public void onInteractBlock(InteractBlockEvent event) {
        if (placeConfig.getValue() && !mc.isInSingleplayer()) {
            event.cancel();
            Managers.NETWORK.sendSequencedPacket(id ->
                    new PlayerInteractBlockC2SPacket(event.getHand(), event.getHitResult(), id));
        }
    }

    /**
     * @param event
     */
    @EventListener
    public void onBreakBlock(BreakBlockEvent event) {
        if (destroyConfig.getValue() && !mc.isInSingleplayer()) {
            event.cancel();
            BlockState state = mc.world.getBlockState(event.getPos());
            state.getBlock().onBreak(mc.world, event.getPos(), state, mc.player);
        }
    }
}
