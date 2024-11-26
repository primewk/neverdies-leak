package org.nrnr.neverdies.api.config.setting;

import org.nrnr.neverdies.Neverdies;
import org.nrnr.neverdies.api.config.ConfigContainer;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.util.render.animation.Animation;

/**
 * @author chronos
 * @see BooleanConfig
 * @since 1.0
 */
public class ToggleConfig extends BooleanConfig {
    public ToggleConfig(String name, String desc, Boolean val) {
        super(name, desc, val);
    }

    /**
     * @param val The param value
     */
    @Override
    public void setValue(Boolean val) {
        super.setValue(val);
        ConfigContainer container = getContainer();
        if (container instanceof ToggleModule toggle) {
            Animation anim = toggle.getAnimation();
            anim.setState(val);
            if (val) {
                Neverdies.EVENT_HANDLER.subscribe(toggle);
            } else {
                Neverdies.EVENT_HANDLER.unsubscribe(toggle);
            }
        }
    }

    public void enable() {
        ConfigContainer container = getContainer();
        if (container instanceof ToggleModule toggle) {
            toggle.enable();
        }
    }

    public void disable() {
        ConfigContainer container = getContainer();
        if (container instanceof ToggleModule toggle) {
            toggle.disable();
        }
    }
}
