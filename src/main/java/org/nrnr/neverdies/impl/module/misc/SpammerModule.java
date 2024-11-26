package org.nrnr.neverdies.impl.module.misc;

import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;

/**
 * @author chronos
 * @since 1.0
 */
public class SpammerModule extends ToggleModule {

    /**
     *
     */
    public SpammerModule() {
        super("Spammer", "Spams messages in the chat", ModuleCategory.MISCELLANEOUS);
    }
}
