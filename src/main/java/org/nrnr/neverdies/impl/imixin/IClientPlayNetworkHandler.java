package org.nrnr.neverdies.impl.imixin;

import net.minecraft.network.packet.Packet;

public interface IClientPlayNetworkHandler {
    void sendQuietPacket(final Packet<?> packet);
}
