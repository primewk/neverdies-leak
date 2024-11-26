package org.nrnr.neverdies.impl.module.combat;

import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.impl.event.AttackCooldownEvent;

public class NoHitDelayModule extends ToggleModule {
    public NoHitDelayModule() {
        super("NoHitDelay", "Removes vanilla attack delay", ModuleCategory.EXPLOITS);
    }

    @EventListener
    public void onAttackCooldown(AttackCooldownEvent event) {
        event.cancel();
    }
}
