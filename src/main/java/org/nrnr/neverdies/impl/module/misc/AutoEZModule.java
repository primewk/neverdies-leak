package org.nrnr.neverdies.impl.module.misc;

import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import net.minecraft.util.math.BlockPos;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.impl.event.entity.EntityDeathEvent;
import org.nrnr.neverdies.impl.event.network.PacketEvent;
import org.nrnr.neverdies.util.chat.ChatUtil;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoEZModule extends ToggleModule {

    public AutoEZModule() {
        super("AutoEz", "Simple AutoEZ module",
                ModuleCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event) {
        if (mc.player == null) {
            return;
        }
        if (event.getPacket() instanceof EntityDeathEvent packet) {
            LivingEntity entity = packet.getEntity();
            String name = String.valueOf(entity.getDisplayName());
            ChatUtil.clientSendMessage(name +" has died");

        }
    }

    @EventListener
    public void onTick(){
        if (mc.player == null || mc.world == null) return;

        for (PlayerEntity otherPlayer : mc.world.getPlayers()) {
            if (otherPlayer.isDead()){
                ChatUtil.clientSendMessage("LOL YOU JUST FUCKING DIED HAHA " + otherPlayer.getDisplayName());
                ChatUtil.serverSendMessage("kek neverdies.me strong " + otherPlayer.getDisplayName());
            }
        }
    }
}
