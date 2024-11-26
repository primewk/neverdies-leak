package org.nrnr.neverdies.impl.module.client;

import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import org.nrnr.neverdies.Neverdies;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ConcurrentModule;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.impl.event.network.PacketEvent;
import org.nrnr.neverdies.init.Managers;

import static net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket.DEMO_MESSAGE_SHOWN;

/**
 * @author xgraza
 * @since 1.0
 */
public final class ServerModule extends ConcurrentModule {
    Config<Boolean> packetKickConfig = new BooleanConfig("NoPacketKick", "Prevents thrown exceptions from kicking you", true);
    Config<Boolean> demoConfig = new BooleanConfig("NoDemo", "Prevents servers from forcing you to a demo screen", true);
    Config<Boolean> resourcePackConfig = new BooleanConfig("NoResourcePack", "Prevents server from forcing resource pack", false);

    public ServerModule() {
        super("Server", "Prevents servers actions on player", ModuleCategory.CLIENT);
    }

    @EventListener
    public void onPacketInbound(final PacketEvent.Inbound event) {
        if (event.getPacket() instanceof GameStateChangeS2CPacket packet) {
            if (packet.getReason() == DEMO_MESSAGE_SHOWN && !mc.isDemo() && demoConfig.getValue()) {
                Neverdies.info("Server attempted to use Demo mode features on you!");
                event.cancel();
            }
        }
        if (event.getPacket() instanceof ResourcePackSendS2CPacket && resourcePackConfig.getValue()) {
            event.cancel();
            Managers.NETWORK.sendPacket(new ResourcePackStatusC2SPacket(mc.player.getUuid(), ResourcePackStatusC2SPacket.Status.DECLINED));
        }
    }

    public boolean isPacketKick() {
        return packetKickConfig.getValue();
    }
}
