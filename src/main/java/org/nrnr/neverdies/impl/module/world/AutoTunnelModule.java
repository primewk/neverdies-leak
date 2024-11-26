package org.nrnr.neverdies.impl.module.world;

import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;

/**
 * @author chronos
 * @since 1.0
 */
public class AutoTunnelModule extends ToggleModule {

    public AutoTunnelModule() {
        super("AutoTunnel", "Automatically mines a tunnel", ModuleCategory.WORLD);
    }
}
