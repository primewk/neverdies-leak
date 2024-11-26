package org.nrnr.neverdies.impl.module.misc;

import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.impl.event.network.DecodePacketEvent;

/**
 * @author chronos
 * @since 1.0
 */
public class NoPacketKickModule extends ToggleModule {

    /**
     *
     */
    public NoPacketKickModule() {
        super("NoPacketKick", "Prevents getting kicked by packets", ModuleCategory.MISCELLANEOUS);
    }

    // TODO: Add more packet kick checks
    @EventListener
    public void onDecodePacket(DecodePacketEvent event) {
        event.cancel();
    }
}
