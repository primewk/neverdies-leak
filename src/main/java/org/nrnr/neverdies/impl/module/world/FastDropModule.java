package org.nrnr.neverdies.impl.module.world;

import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.NumberConfig;
import org.nrnr.neverdies.api.event.EventStage;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.impl.event.TickEvent;
import org.nrnr.neverdies.init.Managers;

/**
 * @author chronos
 * @since 1.0
 */
public class FastDropModule extends ToggleModule {

    Config<Integer> delayConfig = new NumberConfig<>("Delay", "The delay for dropping items", 0, 0, 4);

    private int dropTicks;

    public FastDropModule() {
        super("FastDrop", "Drops items from the hotbar faster", ModuleCategory.WORLD);
    }

    /**
     * @param event
     */
    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() != EventStage.PRE) {
            return;
        }
        if (mc.options.dropKey.isPressed() && dropTicks > delayConfig.getValue()) {
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.DROP_ITEM,
                    BlockPos.ORIGIN, Direction.DOWN));
            dropTicks = 0;
        }
        ++dropTicks;
    }
}
