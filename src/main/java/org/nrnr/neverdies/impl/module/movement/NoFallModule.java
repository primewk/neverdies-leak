package org.nrnr.neverdies.impl.module.movement;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.world.World;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.EnumConfig;
import org.nrnr.neverdies.api.event.EventStage;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.impl.event.network.PacketEvent;
import org.nrnr.neverdies.impl.event.network.PlayerUpdateEvent;
import org.nrnr.neverdies.init.Managers;
import org.nrnr.neverdies.init.Modules;
import org.nrnr.neverdies.mixin.accessor.AccessorPlayerMoveC2SPacket;
import org.nrnr.neverdies.util.string.EnumFormatter;

/**
 * @author chronos
 * @since 1.0
 */
public class NoFallModule extends ToggleModule {

    //
    Config<NoFallMode> modeConfig = new EnumConfig<>("Mode", "The mode to prevent fall damage", NoFallMode.ANTI, NoFallMode.values());

    /**
     *
     */
    public NoFallModule() {
        super("NoFall", "Prevents all fall damage", ModuleCategory.MOVEMENT);
    }

    @Override
    public String getModuleData() {
        return EnumFormatter.formatEnum(modeConfig.getValue());
    }

    @EventListener
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (event.getStage() != EventStage.PRE || !checkFalling()) {
            return;
        }
        if (modeConfig.getValue() == NoFallMode.LATENCY) {
            if (mc.world.getRegistryKey() == World.NETHER) {
                Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        mc.player.getX(), 0, mc.player.getZ(), true));
            } else {
                Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(0, 64, 0, true));
            }
            mc.player.fallDistance = 0.0f;
        } else if (modeConfig.getValue() == NoFallMode.GRIM) {
            Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY() + 1.0e-9,
                    mc.player.getZ(), mc.player.getYaw(), mc.player.getPitch(), true));
            mc.player.onLanding();
        }
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event) {
        if (mc.player == null || !checkFalling()) {
            return;
        }
        if (event.getPacket() instanceof PlayerMoveC2SPacket packet) {
            if (modeConfig.getValue() == NoFallMode.PACKET) {
                ((AccessorPlayerMoveC2SPacket) packet).hookSetOnGround(true);
            } else if (modeConfig.getValue() == NoFallMode.ANTI) {
                double y = packet.getY(mc.player.getY());
                ((AccessorPlayerMoveC2SPacket) packet).hookSetY(y + 0.10000000149011612);
            }
        }
    }

    private boolean checkFalling() {
        return mc.player.fallDistance > mc.player.getSafeFallDistance() && !mc.player.isOnGround()
                && !mc.player.isFallFlying() && !Modules.FLIGHT.isEnabled() && !Modules.PACKET_FLY.isEnabled();
    }

    public enum NoFallMode {
        ANTI,
        LATENCY,
        PACKET,
        GRIM
    }
}
