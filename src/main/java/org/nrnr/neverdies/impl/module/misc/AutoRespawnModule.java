package org.nrnr.neverdies.impl.module.misc;

import net.minecraft.client.gui.screen.DeathScreen;
import org.nrnr.neverdies.api.event.EventStage;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.impl.event.ScreenOpenEvent;
import org.nrnr.neverdies.impl.event.TickEvent;

/**
 * @author chronos
 * @since 1.0
 */
public class AutoRespawnModule extends ToggleModule {
    //
    private boolean respawn;

    /**
     *
     */
    public AutoRespawnModule() {
        super("AutoRespawn", "Respawns automatically after a death",
                ModuleCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() == EventStage.PRE && respawn && mc.player.isDead()) {
            mc.player.requestRespawn();
            respawn = false;
        }
    }

    @EventListener
    public void onScreenOpen(ScreenOpenEvent event) {
        if (event.getScreen() instanceof DeathScreen) {
            respawn = true;
        }
    }
}
