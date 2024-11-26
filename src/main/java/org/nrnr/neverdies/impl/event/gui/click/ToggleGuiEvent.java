package org.nrnr.neverdies.impl.event.gui.click;

import org.nrnr.neverdies.api.event.Event;
import org.nrnr.neverdies.api.module.ToggleModule;

/**
 * @author chronos
 * @since 1.0
 */
public class ToggleGuiEvent extends Event {
    private final ToggleModule module;

    public ToggleGuiEvent(ToggleModule module) {
        this.module = module;
    }

    public ToggleModule getModule() {
        return module;
    }

    public boolean isEnabled() {
        return module.isEnabled();
    }
}
