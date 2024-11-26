package org.nrnr.neverdies.impl.module.misc;

import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.impl.event.network.PacketEvent;

/**
 * @author chronos
 * @since 1.0
 */
public class XCarryModule extends ToggleModule {
    //
    Config<Boolean> inventoryConfig = new BooleanConfig("Inventory", "Prevents server from recieving packets regarding inventory items", true);
    Config<Boolean> armorConfig = new BooleanConfig("Armor", "Prevents server from recieving packets regarding armor items", false);
    Config<Boolean> forceCancelConfig = new BooleanConfig("ForceCancel", "Cancels all close window packets", false);

    /**
     *
     */
    public XCarryModule() {
        super("XCarry", "Allow player to carry items in the crafting slots",
                ModuleCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event) {
        if (mc.player == null) {
            return;
        }
        if (event.getPacket() instanceof CloseHandledScreenC2SPacket packet
                && (packet.getSyncId() == mc.player.playerScreenHandler.syncId
                || forceCancelConfig.getValue())) {
            event.cancel();
        }
    }
}
