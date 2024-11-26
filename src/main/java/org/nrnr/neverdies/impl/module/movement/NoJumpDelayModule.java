package org.nrnr.neverdies.impl.module.movement;

import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.impl.event.entity.JumpDelayEvent;

public class NoJumpDelayModule extends ToggleModule {
    public NoJumpDelayModule() {
        super("NoJumpDelay", "Removes the vanilla jump delay", ModuleCategory.MOVEMENT);
    }

    @EventListener
    public void onJumpDelay(JumpDelayEvent event) {
        event.cancel();
    }
}
