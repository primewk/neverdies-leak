package org.nrnr.neverdies.impl.module.misc;

import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.util.hit.BlockHitResult;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.impl.event.network.PacketEvent;
import org.nrnr.neverdies.impl.imixin.IPlayerInteractEntityC2SPacket;

public class PacketLoggerModule extends ToggleModule {

    Config<Boolean> chatConfig = new BooleanConfig("LogChat", "Logs packets in the chats", false);
    Config<Boolean> moveFullConfig = new BooleanConfig("PlayerMoveFull", "Logs PlayerMoveC2SPacket", false);
    Config<Boolean> moveLookConfig = new BooleanConfig("PlayerMoveLook", "Logs PlayerMoveC2SPacket", false);
    Config<Boolean> movePosConfig = new BooleanConfig("PlayerMovePosition", "Logs PlayerMoveC2SPacket", false);
    Config<Boolean> moveGroundConfig = new BooleanConfig("PlayerMoveGround", "Logs PlayerMoveC2SPacket", false);
    Config<Boolean> vehicleMoveConfig = new BooleanConfig("VehicleMove", "Logs VehicleMoveC2SPacket", false);
    Config<Boolean> playerActionConfig = new BooleanConfig("PlayerAction", "Logs PlayerActionC2SPacket", false);
    Config<Boolean> updateSlotConfig = new BooleanConfig("UpdateSelectedSlot", "Logs UpdateSelectedSlotC2SPacket", false);
    Config<Boolean> clickSlotConfig = new BooleanConfig("ClickSlot", "Logs ClickSlotC2SPacket", false);
    Config<Boolean> pickInventoryConfig = new BooleanConfig("PickInventory", "Logs PickFromInventoryC2SPacket", false);
    Config<Boolean> handSwingConfig = new BooleanConfig("HandSwing", "Logs HandSwingC2SPacket", false);
    Config<Boolean> interactEntityConfig = new BooleanConfig("InteractEntity", "Logs PlayerInteractEntityC2SPacket", false);
    Config<Boolean> interactBlockConfig = new BooleanConfig("InteractBlock", "Logs PlayerInteractBlockC2SPacket", false);
    Config<Boolean> interactItemConfig = new BooleanConfig("InteractItem", "Logs PlayerInteractItemC2SPacket", false);
    Config<Boolean> commandConfig = new BooleanConfig("ClientCommand", "Logs ClientCommandC2SPacket", false);
    Config<Boolean> statusConfig = new BooleanConfig("ClientStatus", "Logs ClientStatusC2SPacket", false);
    Config<Boolean> closeScreenConfig = new BooleanConfig("CloseScreen", "Logs CloseHandledScreenC2SPacket", false);
    Config<Boolean> teleportConfirmConfig = new BooleanConfig("TeleportConfirm", "Logs TeleportConfirmC2SPacket", false);
    Config<Boolean> pongConfig = new BooleanConfig("Pong", "Logs CommonPongC2SPacket", false);

    public PacketLoggerModule() {
        super("PacketLogger", "Logs client packets", ModuleCategory.MISCELLANEOUS);
    }

    private void logPacket(String msg, Object... args) {
        String s = String.format(msg, args);
        if (chatConfig.getValue()) {
            sendModuleMessage(s);
        } else {
            System.out.println(s);
        }
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event) {
        if (event.getPacket() instanceof PlayerMoveC2SPacket.Full packet && moveFullConfig.getValue()) {
            StringBuilder builder = new StringBuilder();
            builder.append("PlayerMove Full - ");
            if (packet.changesPosition()) {
                builder.append("x: ").append(packet.getX(0.0)).append(", y: ").append(packet.getY(0.0)).append(", z: ").append(packet.getZ(0.0)).append(" ");
            }
            if (packet.changesLook()) {
                builder.append("yaw: ").append(packet.getYaw(0.0f)).append(", pitch: ").append(packet.getPitch(0.0f)).append(" ");
            }
            builder.append(" onground: ").append(packet.isOnGround());
            logPacket(builder.toString());
        }
        if (event.getPacket() instanceof PlayerMoveC2SPacket.PositionAndOnGround packet && movePosConfig.getValue()) {
            StringBuilder builder = new StringBuilder();
            builder.append("PlayerMove PosGround - ");
            if (packet.changesPosition()) {
                builder.append("x: ").append(packet.getX(0.0)).append(", y: ").append(packet.getY(0.0)).append(", z: ").append(packet.getZ(0.0)).append(" ");
            }
            builder.append(" onground: ").append(packet.isOnGround());
            logPacket(builder.toString());
        }
        if (event.getPacket() instanceof PlayerMoveC2SPacket.LookAndOnGround packet && moveLookConfig.getValue()) {
            StringBuilder builder = new StringBuilder();
            builder.append("PlayerMove LookGround - ");
            if (packet.changesLook()) {
                builder.append("yaw: ").append(packet.getYaw(0.0f)).append(", pitch: ").append(packet.getPitch(0.0f)).append(" ");
            }
            builder.append(" onground: ").append(packet.isOnGround());
            logPacket(builder.toString());
        }
        if (event.getPacket() instanceof PlayerMoveC2SPacket.OnGroundOnly packet && moveGroundConfig.getValue()) {
            String s = "PlayerMove Ground - onground: " + packet.isOnGround();
            logPacket(s);
        }
        if (event.getPacket() instanceof VehicleMoveC2SPacket packet && vehicleMoveConfig.getValue()) {
            logPacket("VehicleMove - x: %s, y: %s, z: %s, yaw: %s, pitch: %s", packet.getX(), packet.getY(), packet.getZ(), packet.getYaw(), packet.getPitch());
        }
        if (event.getPacket() instanceof PlayerActionC2SPacket packet && playerActionConfig.getValue()) {
            logPacket("PlayerAction - action: %s, direction: %s, pos: %s", packet.getAction().name(), packet.getDirection().name(), packet.getPos().toShortString());
        }
        if (event.getPacket() instanceof UpdateSelectedSlotC2SPacket packet && updateSlotConfig.getValue()) {
            logPacket("UpdateSlot - slot: %d", packet.getSelectedSlot());
        }
        if (event.getPacket() instanceof HandSwingC2SPacket packet && handSwingConfig.getValue()) {
            logPacket("HandSwing - hand: %s", packet.getHand().name());
        }
        if (event.getPacket() instanceof CommonPongC2SPacket packet && pongConfig.getValue()) {
            logPacket("Pong - %d", packet.getParameter());
        }
        if (event.getPacket() instanceof IPlayerInteractEntityC2SPacket packet && interactEntityConfig.getValue()) {
            logPacket("InteractEntity - %s", packet.getEntity().getName().getString());
        }
        if (event.getPacket() instanceof PlayerInteractBlockC2SPacket packet && interactBlockConfig.getValue()) {
            BlockHitResult blockHitResult = packet.getBlockHitResult();
            logPacket("InteractBlock - pos: %s, dir: %s, hand: %s", blockHitResult.getBlockPos().toShortString(), blockHitResult.getSide().name(), packet.getHand().name());
        }
        if (event.getPacket() instanceof PlayerInteractItemC2SPacket packet && interactItemConfig.getValue()) {
            logPacket("InteractItem - hand: %s", packet.getHand().name());
        }
        if (event.getPacket() instanceof CloseHandledScreenC2SPacket packet && closeScreenConfig.getValue()) {
            logPacket("CloseScreen - id: %s", packet.getSyncId());
        }
        if (event.getPacket() instanceof ClientCommandC2SPacket packet && commandConfig.getValue()) {
            logPacket("ClientCommand - mode: %s", packet.getMode().name());
        }
        if (event.getPacket() instanceof ClientStatusC2SPacket packet && statusConfig.getValue()) {
            logPacket("ClientStatus - mode: %s", packet.getMode().name());
        }
        if (event.getPacket() instanceof ClickSlotC2SPacket packet && clickSlotConfig.getValue()) {
            logPacket("ClickSlot - type: %s, slot: %s, button: %s, id: %s", packet.getActionType().name(), packet.getSlot(), packet.getButton(), packet.getSyncId());
        }
        if (event.getPacket() instanceof PickFromInventoryC2SPacket packet && pickInventoryConfig.getValue()) {
            logPacket("PickInventory - slot: %s", packet.getSlot());
        }
        if (event.getPacket() instanceof TeleportConfirmC2SPacket packet && teleportConfirmConfig.getValue()) {
            logPacket("TeleportConfirm - id: %s", packet.getTeleportId());
        }
    }
}
