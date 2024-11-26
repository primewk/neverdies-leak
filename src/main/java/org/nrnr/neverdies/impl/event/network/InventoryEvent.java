package org.nrnr.neverdies.impl.event.network;

import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import org.nrnr.neverdies.api.event.Event;

public class InventoryEvent extends Event {
    private final InventoryS2CPacket packet;

    public InventoryEvent(InventoryS2CPacket packet) {
        this.packet = packet;
    }

    public InventoryS2CPacket getPacket() {
        return packet;
    }
}
