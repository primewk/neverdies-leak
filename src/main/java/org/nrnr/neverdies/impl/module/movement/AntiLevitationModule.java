package org.nrnr.neverdies.impl.module.movement;

import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.impl.event.entity.LevitationEvent;

/**
 * @author chronos
 * @since 1.0
 */
public class AntiLevitationModule extends ToggleModule {

    /**
     *
     */
    public AntiLevitationModule() {
        super("AntiLevitation", "Prevents the player from being levitated",
                ModuleCategory.MOVEMENT);
    }

    @EventListener
    public void onLevitation(LevitationEvent event) {
        event.cancel();
    }
}
