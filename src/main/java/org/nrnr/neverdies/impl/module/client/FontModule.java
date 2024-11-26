package org.nrnr.neverdies.impl.module.client;

import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;

/**
 * @author chronos
 * @since 1.0
 */
public class FontModule extends ToggleModule {
    //
    Config<Boolean> shadowConfig = new BooleanConfig("Shadow", "Renders text with a shadow background", true);

    /**
     *
     */
    public FontModule() {
        super("Font", "Changes the client text to custom font rendering",
                ModuleCategory.CLIENT);
    }

    /**
     * @return
     */
    public boolean getShadow() {
        return shadowConfig.getValue();
    }
}
