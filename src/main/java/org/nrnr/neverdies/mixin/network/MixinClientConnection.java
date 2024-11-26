package org.nrnr.neverdies.mixin.network;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.Text;
import org.nrnr.neverdies.Neverdies;
import org.nrnr.neverdies.impl.event.network.DisconnectEvent;
import org.nrnr.neverdies.impl.event.network.PacketEvent;
import org.nrnr.neverdies.init.Modules;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author chronos
 * @since 1.0
 */
@Mixin(ClientConnection.class)
public class MixinClientConnection {
    @Shadow
    @Nullable
    private volatile PacketListener packetListener;

    @Shadow
    @Final
    private static Logger LOGGER;

    @Inject(method = "exceptionCaught", at = @At("HEAD"), cancellable = true)
    private void hookExceptionCaught(ChannelHandlerContext context, Throwable ex, CallbackInfo ci) {
        if (Modules.SERVER.isPacketKick()) {
            LOGGER.error("Exception caught on network thread:", ex);
            ci.cancel();
        }
    }

    /**
     * @param packet
     * @param callbacks
     * @param ci
     */
    @Inject(method = "sendImmediately", at = @At(value = "HEAD"),
            cancellable = true)
    private void hookSendImmediately(Packet<?> packet, @Nullable PacketCallbacks callbacks,
                                     boolean flush, CallbackInfo ci) {
        PacketEvent.Outbound packetOutboundEvent =
                new PacketEvent.Outbound(packet);
        Neverdies.EVENT_HANDLER.dispatch(packetOutboundEvent);
        if (packetOutboundEvent.isCanceled()) {
            ci.cancel();
        }
    }

    /**
     * @param channelHandlerContext
     * @param packet
     * @param ci
     */
    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;" +
            "Lnet/minecraft/network/packet/Packet;)V", at = @At(value = "HEAD"),
            cancellable = true)
    private void hookChannelRead0(ChannelHandlerContext channelHandlerContext,
                                  Packet<?> packet, CallbackInfo ci) {
        PacketEvent.Inbound packetInboundEvent =
                new PacketEvent.Inbound(packetListener, packet);
        Neverdies.EVENT_HANDLER.dispatch(packetInboundEvent);
        // prevent client from receiving packet from server
        if (packetInboundEvent.isCanceled()) {
            ci.cancel();
        }
    }

    /**
     * @param disconnectReason
     * @param ci
     */
    @Inject(method = "disconnect", at = @At(value = "HEAD"))
    private void hookDisconnect(Text disconnectReason, CallbackInfo ci) {
        DisconnectEvent disconnectEvent = new DisconnectEvent();
        Neverdies.EVENT_HANDLER.dispatch(disconnectEvent);
    }
}
