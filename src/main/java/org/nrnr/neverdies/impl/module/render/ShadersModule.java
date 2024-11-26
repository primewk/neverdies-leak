package org.nrnr.neverdies.impl.module.render;

import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;

/**
 * @author chronos
 * @since 1.0
 */
public class ShadersModule extends ToggleModule {

    public ShadersModule() {
        super("Shaders", "Renders shaders over entities", ModuleCategory.RENDER);
    }
}
