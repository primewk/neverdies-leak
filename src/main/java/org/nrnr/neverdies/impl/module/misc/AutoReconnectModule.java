package org.nrnr.neverdies.impl.module.misc;

import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.NumberConfig;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.mixin.gui.screen.MixinDisconnectedScreen;

/**
 * @author chronos
 * @see MixinDisconnectedScreen
 * @since 1.0
 */
public class AutoReconnectModule extends ToggleModule {
    //
    Config<Integer> delayConfig = new NumberConfig<>("Delay", "The delay between reconnects to a server", 0, 5, 100);

    /**
     *
     */
    public AutoReconnectModule() {
        super("AutoReconnect", "Automatically reconnects to a server " +
                "immediately after disconnecting", ModuleCategory.MISCELLANEOUS);
    }

    /**
     * @return
     */
    public int getDelay() {
        return delayConfig.getValue();
    }
}
