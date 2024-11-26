package org.nrnr.neverdies.impl.manager.network;

import net.minecraft.client.network.*;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import org.nrnr.neverdies.Neverdies;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.impl.event.network.DisconnectEvent;
import org.nrnr.neverdies.impl.imixin.IClientPlayNetworkHandler;
import org.nrnr.neverdies.mixin.accessor.AccessorClientWorld;
import org.nrnr.neverdies.util.Globals;

import java.util.HashSet;
import java.util.Set;

/**
 * @author chronos
 * @since 1.0
 */
public class NetworkManager implements Globals {
    //
    private static final Set<Packet<?>> PACKET_CACHE = new HashSet<>();
    //
    private ServerAddress address;
    private ServerInfo info;

    /**
     *
     */
    public NetworkManager() {
        Neverdies.EVENT_HANDLER.subscribe(this);
    }

    /**
     * @param event
     */
    @EventListener
    public void onDisconnect(DisconnectEvent event) {
        PACKET_CACHE.clear();
    }

    /**
     * @param p
     */
    public void sendPacket(final Packet<?> p) {
        if (mc.getNetworkHandler() != null) {
            PACKET_CACHE.add(p);
            mc.getNetworkHandler().sendPacket(p);
        }
    }

    public void sendQuietPacket(final Packet<?> p) {
        if (mc.getNetworkHandler() != null) {
            PACKET_CACHE.add(p);
            ((IClientPlayNetworkHandler) mc.getNetworkHandler()).sendQuietPacket(p);
        }
    }

    /**
     * @param p
     */
    public void sendSequencedPacket(final SequencedPacketCreator p) {
        if (mc.world != null) {
            PendingUpdateManager updater =
                    ((AccessorClientWorld) mc.world).hookGetPendingUpdateManager().incrementSequence();
            try {
                int i = updater.getSequence();
                Packet<ServerPlayPacketListener> packet = p.predict(i);
                sendPacket(packet);
            } catch (Throwable e) {
                e.printStackTrace();
                if (updater != null) {
                    try {
                        updater.close();
                    } catch (Throwable e1) {
                        e1.printStackTrace();
                        e.addSuppressed(e1);
                    }
                }
                throw e;
            }
            if (updater != null) {
                updater.close();
            }
        }
    }

    /**
     * @return
     */
    public int getClientLatency() {
        if (mc.getNetworkHandler() != null) {
            final PlayerListEntry playerEntry =
                    mc.getNetworkHandler().getPlayerListEntry(mc.player.getGameProfile().getId());
            if (playerEntry != null) {
                return playerEntry.getLatency();
            }
        }
        return 0;
    }

    public ServerAddress getAddress() {
        return address;
    }

    public void setAddress(ServerAddress address) {
        this.address = address;
    }

    public ServerInfo getInfo() {
        return info;
    }

    public void setInfo(ServerInfo info) {
        this.info = info;
    }

    public boolean isCrystalPvpCC() {
        if (info != null) {
            return info.address.equalsIgnoreCase("us.crystalpvp.cc") || info.address.equalsIgnoreCase("crystalpvp.cc");
        }
        return false;
    }

    public boolean isGrimCC() {
        return info != null && info.address.equalsIgnoreCase("grim.crystalpvp.cc");
    }

    /**
     * @param p
     * @return
     */
    public boolean isCached(Packet<?> p) {
        return PACKET_CACHE.contains(p);
    }
}
