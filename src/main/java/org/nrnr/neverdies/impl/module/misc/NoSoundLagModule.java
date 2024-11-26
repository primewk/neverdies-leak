package org.nrnr.neverdies.impl.module.misc;

import net.minecraft.network.packet.s2c.play.PlaySoundFromEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.impl.event.network.PacketEvent;

import java.util.Set;

/**
 * @author chronos
 * @since 1.0
 */
public class NoSoundLagModule extends ToggleModule {
    //
    private final static Set<SoundEvent> LAG_SOUNDS = Set.of(
            SoundEvents.ITEM_ARMOR_EQUIP_GENERIC,
            SoundEvents.ITEM_ARMOR_EQUIP_ELYTRA,
            SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE,
            SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND,
            SoundEvents.ITEM_ARMOR_EQUIP_IRON,
            SoundEvents.ITEM_ARMOR_EQUIP_GOLD,
            SoundEvents.ITEM_ARMOR_EQUIP_CHAIN,
            SoundEvents.ITEM_ARMOR_EQUIP_LEATHER
    );

    /**
     *
     */
    public NoSoundLagModule() {
        super("NoSoundLag", "Prevents sound effects from lagging the game",
                ModuleCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event) {
        if (event.getPacket() instanceof PlaySoundFromEntityS2CPacket packet
                && LAG_SOUNDS.contains(packet.getSound().value())
                || event.getPacket() instanceof PlaySoundS2CPacket packet2
                && LAG_SOUNDS.contains(packet2.getSound().value())) {
            event.cancel();
        }
    }
}
