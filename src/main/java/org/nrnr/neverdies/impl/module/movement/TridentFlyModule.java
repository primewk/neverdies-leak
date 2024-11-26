package org.nrnr.neverdies.impl.module.movement;

import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.config.setting.NumberConfig;
import org.nrnr.neverdies.api.event.EventStage;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.impl.event.TickEvent;
import org.nrnr.neverdies.impl.event.item.TridentPullbackEvent;
import org.nrnr.neverdies.impl.event.item.TridentWaterEvent;
import org.nrnr.neverdies.init.Managers;

/**
 * @author Neverdies
 * @since 1.0
 */
public class TridentFlyModule extends ToggleModule {

    Config<Boolean> allowNoWaterConfig = new BooleanConfig("AllowNoWater", "Allows you to fly using tridents even without water", true);
    Config<Boolean> instantConfig = new BooleanConfig("Instant", "Removes the pullback of the trident", true);
    Config<Boolean> flyConfig = new BooleanConfig("Spam", "Automatically uses riptide", false);
    Config<Integer> ticksConfig = new NumberConfig<>("Ticks", "The ticks between riptide boost", 0, 3, 20, () -> flyConfig.getValue());

    public TridentFlyModule() {
        super("TridentFly", "Allows you to fly using tridents", ModuleCategory.MOVEMENT);
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() == EventStage.PRE || !flyConfig.getValue()) {
            return;
        }
        if (mc.player.getMainHandStack().getItem() == Items.TRIDENT && mc.player.getItemUseTime() >= ticksConfig.getValue()) {
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));
            mc.player.stopUsingItem();
        }
    }

    @EventListener
    public void onTridentPullback(TridentPullbackEvent event) {
        if (instantConfig.getValue()) {
            event.cancel();
        }
    }

    @EventListener
    public void onTridentWaterCheck(TridentWaterEvent event) {
        if (allowNoWaterConfig.getValue()) {
            event.cancel();
        }
    }
}
