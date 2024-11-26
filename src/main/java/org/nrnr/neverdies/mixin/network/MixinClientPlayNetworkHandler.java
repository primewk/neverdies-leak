package org.nrnr.neverdies.mixin.network;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import org.nrnr.neverdies.Neverdies;
import org.nrnr.neverdies.impl.event.gui.chat.ChatMessageEvent;
import org.nrnr.neverdies.impl.event.network.GameJoinEvent;
import org.nrnr.neverdies.impl.event.network.InventoryEvent;
import org.nrnr.neverdies.impl.imixin.IClientPlayNetworkHandler;
import org.nrnr.neverdies.mixin.accessor.AccessorClientConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author chronos
 * @since 1.0
 */
@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler implements IClientPlayNetworkHandler {
    @Shadow
    public abstract ClientConnection getConnection();

    /**
     * @param content
     * @param ci
     */
    @Inject(method = "sendChatMessage", at = @At(value = "HEAD"),
            cancellable = true)
    private void hookSendChatMessage(String content, CallbackInfo ci) {
        ChatMessageEvent.Server chatInputEvent =
                new ChatMessageEvent.Server(content);
        Neverdies.EVENT_HANDLER.dispatch(chatInputEvent);
        // prevent chat packet from sending
        if (chatInputEvent.isCanceled()) {
            ci.cancel();
        }
    }

    /**
     * @param packet
     * @param ci
     */
    @Inject(method = "onGameJoin", at = @At(value = "TAIL"))
    private void hookOnGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        GameJoinEvent gameJoinEvent = new GameJoinEvent();
        Neverdies.EVENT_HANDLER.dispatch(gameJoinEvent);
    }

    /**
     * @param packet
     * @param ci
     */
    @Inject(method = "onInventory", at = @At(value = "TAIL"))
    private void hookOnInventory(InventoryS2CPacket packet, CallbackInfo ci) {
        InventoryEvent inventoryEvent = new InventoryEvent(packet);
        Neverdies.EVENT_HANDLER.dispatch(inventoryEvent);
    }

    @Override
    public void sendQuietPacket(Packet<?> packet) {
        ((AccessorClientConnection) getConnection()).hookSendInternal(packet, null, true);
    }
}
