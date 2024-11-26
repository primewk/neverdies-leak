package org.nrnr.neverdies.impl.module.misc;

import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.NumberConfig;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.impl.event.network.PacketEvent;
import org.nrnr.neverdies.init.Managers;
import org.nrnr.neverdies.util.chat.ChatUtil;
import org.nrnr.neverdies.util.math.timer.CacheTimer;
import org.nrnr.neverdies.util.math.timer.Timer;

/**
 * @author chronos
 * @since 1.0
 */
public class AutoAcceptModule extends ToggleModule {
    //
    private final Timer acceptTimer = new CacheTimer();
    //
    Config<Float> delayConfig = new NumberConfig<>("Delay", "The delay before" +
            " accepting teleport requests", 0.0f, 3.0f, 10.0f);

    /**
     *
     */
    public AutoAcceptModule() {
        super("AutoAccept", "Automatically accepts teleport requests",
                ModuleCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event) {
        if (event.getPacket() instanceof ChatMessageS2CPacket packet) {
            String text = packet.body().content();
            if ((text.contains("has requested to teleport to you.")
                    || text.contains("has requested you teleport to them."))
                    && acceptTimer.passed(delayConfig.getValue() * 1000)) {
                for (String friend : Managers.SOCIAL.getFriends()) {
                    if (text.contains(friend)) {
                        ChatUtil.serverSendMessage("/tpaccept");
                        break;
                    }
                }
            }
        }
    }
}
