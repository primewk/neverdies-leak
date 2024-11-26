package org.nrnr.neverdies.impl.module.legit;

import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.config.setting.NumberConfig;
import org.nrnr.neverdies.api.config.setting.StringConfig;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.impl.event.network.PacketEvent;
import org.nrnr.neverdies.util.chat.ChatUtil;

/**
 * @author chronos
 * @since 1.0
 */
public class AntiAFKModule extends ToggleModule {
    //
    Config<Boolean> messageConfig = new BooleanConfig("Message", "Messages in chat to prevent AFK kick", true);
    Config<Boolean> tabCompleteConfig = new BooleanConfig("TabComplete", "Uses tab complete in chat to prevent AFK kick", true);
    Config<Boolean> rotateConfig = new BooleanConfig("Rotate", "Rotates the player to prevent AFK kick", true);
    Config<Boolean> autoReplyConfig = new BooleanConfig("AutoReply", "Replies to players messaging you in chat", true);
    Config<String> replyConfig = new StringConfig("Reply", "The reply message for AutoReply", "[Neverdies] I am currently AFK.");
    Config<Float> delayConfig = new NumberConfig<>("Delay", "The delay between actions", 5.0f, 60.0f, 270.0f);

    /**
     *
     */
    public AntiAFKModule() {
        super("AntiAFK", "Prevents the player from being kicked for AFK",
                ModuleCategory.LEGIT);
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event) {
        if (event.getPacket() instanceof ChatMessageS2CPacket packet
                && autoReplyConfig.getValue()) {
            String[] words = packet.body().content().split(" ");
            if (words[1].startsWith("whispers:")) {
                ChatUtil.serverSendMessage("/r " + replyConfig.getValue());
            }
        }
    }
}
