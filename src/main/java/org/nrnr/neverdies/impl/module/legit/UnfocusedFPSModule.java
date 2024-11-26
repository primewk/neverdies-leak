package org.nrnr.neverdies.impl.module.legit;

import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.NumberConfig;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.impl.event.FramerateLimitEvent;

/**
 * @author chronos
 * @since 1.0
 */
public class UnfocusedFPSModule extends ToggleModule {
    //
    Config<Integer> limitConfig = new NumberConfig<>("Limit", "The FPS limit when game is in the background", 5, 30, 120);

    /**
     *
     */
    public UnfocusedFPSModule() {
        super("UnfocusedFPS", "Reduces FPS when game is in the background",
                ModuleCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onFramerateLimit(FramerateLimitEvent event) {
        if (!mc.isWindowFocused()) {
            event.cancel();
            event.setFramerateLimit(limitConfig.getValue());
        }
    }
}
