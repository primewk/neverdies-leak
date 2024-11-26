package org.nrnr.neverdies.impl.module.misc;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.impl.event.entity.EntityDeathEvent;
import org.nrnr.neverdies.impl.event.network.GameJoinEvent;
import org.nrnr.neverdies.impl.event.network.PacketEvent;
import org.nrnr.neverdies.impl.event.world.AddEntityEvent;
import org.nrnr.neverdies.impl.event.world.RemoveEntityEvent;
import org.nrnr.neverdies.init.Managers;
import org.nrnr.neverdies.util.chat.ChatUtil;

public class ChatNotifierModule extends ToggleModule {

    Config<Boolean> moduleNotifConfig = new BooleanConfig("ModuleNotifications", "Notifies in chat when a module has been toggled", true);
    Config<Boolean> totemPopConfig = new BooleanConfig("TotemPop", "Notifies in chat when a player pops a totem", true);
    Config<Boolean> visualRangeConfig = new BooleanConfig("VisualRange", "Notifies in chat when player enters visual range", false);
    Config<Boolean> friendsConfig = new BooleanConfig("Friends", "Notifies for friends", false);
    Config<Boolean> grimConfig = new BooleanConfig("Grim", "Notifies you if the server you join is running GrimAC", false);

    public ChatNotifierModule() {
        super("ChatNotifier", "Notifies in chat", ModuleCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event) {
        if (event.getPacket() instanceof EntityStatusS2CPacket packet && packet.getStatus() == EntityStatuses.USE_TOTEM_OF_UNDYING && totemPopConfig.getValue()) {
            Entity entity = packet.getEntity(mc.world);
            if (!(entity instanceof LivingEntity) || entity.getDisplayName() == null) {
                return;
            }
            int totems = Managers.TOTEM.getTotems(entity);
            String playerName = entity.getDisplayName().getString();
            boolean isFriend = Managers.SOCIAL.isFriend(playerName);
            if (isFriend && !friendsConfig.getValue() || entity == mc.player) {
                return;
            }
            ChatUtil.clientSendMessage((isFriend ? "§b" : "§s") + playerName + "§f popped §s" + totems + "§f totems");
        }
    }



    @EventListener
    public void onGameJoin(GameJoinEvent event) {
        if (grimConfig.getValue()) {
            if (Managers.ANTICHEAT.isGrim()) {
                ChatUtil.clientSendMessage("This server is running GrimAC.");
            } else {
                ChatUtil.clientSendMessage("This server is not running GrimAC.");
            }
        }
    }

    @EventListener
    public void onAddEntity(AddEntityEvent event) {
        if (!visualRangeConfig.getValue() || !(event.getEntity() instanceof PlayerEntity) || event.getEntity().getDisplayName() == null) {
            return;
        }
        String playerName = event.getEntity().getDisplayName().getString();
        boolean isFriend = Managers.SOCIAL.isFriend(playerName);
        if (isFriend && !friendsConfig.getValue() || event.getEntity() == mc.player) {
            return;
        }
        ChatUtil.clientSendMessageRaw("§s[VisualRange] " + (isFriend ? "§b" + playerName : playerName) + "§f entered your visual range");
    }

    @EventListener
    public void onRemoveEntity(RemoveEntityEvent event) {
        if (!visualRangeConfig.getValue() || !(event.getEntity() instanceof PlayerEntity) || event.getEntity().getDisplayName() == null) {
            return;
        }
        String playerName = event.getEntity().getDisplayName().getString();
        boolean isFriend = Managers.SOCIAL.isFriend(playerName);
        if (isFriend && !friendsConfig.getValue() || event.getEntity() == mc.player) {
            return;
        }
        ChatUtil.clientSendMessageRaw("§s[VisualRange] " + (isFriend ? "§b" + playerName : "§c" + playerName) + "§f left your visual range");
    }

    @EventListener
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getDisplayName() == null || !totemPopConfig.getValue()) {
            return;
        }
        int totems = Managers.TOTEM.getTotems(event.getEntity());
        if (totems == 0) {
            return;
        }
        String playerName = event.getEntity().getDisplayName().getString();
        boolean isFriend = Managers.SOCIAL.isFriend(playerName);
        if (isFriend && !friendsConfig.getValue() || event.getEntity() == mc.player) {
            return;
        }
        ChatUtil.clientSendMessage((isFriend ? "§b" : "§s") + playerName + "§f died after popping §s" + totems + "§f totems");
    }
}
