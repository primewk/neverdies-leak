package org.nrnr.neverdies.impl.module.world;

import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.impl.event.ItemMultitaskEvent;

/**
 * @author chronos
 * @since 1.0
 */
public class MultitaskModule extends ToggleModule {

    public MultitaskModule() {
        super("MultiTask", "Allows you to mine and use items simultaneously", ModuleCategory.WORLD);
    }

    @EventListener
    public void onItemMultitask(ItemMultitaskEvent event) {
        event.cancel();
    }
}
