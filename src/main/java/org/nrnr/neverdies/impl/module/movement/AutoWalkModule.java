package org.nrnr.neverdies.impl.module.movement;

import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.event.EventStage;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.impl.event.TickEvent;

/**
 * @author chronos
 * @since 1.0
 */
public class AutoWalkModule extends ToggleModule {
    //
    Config<Boolean> lockConfig = new BooleanConfig("Lock", "Stops movement when sneaking or jumping", false);

    /**
     *
     */
    public AutoWalkModule() {
        super("AutoWalk", "Automatically moves forward", ModuleCategory.MOVEMENT);
    }

    @Override
    public void onDisable() {
        mc.options.forwardKey.setPressed(false);
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() == EventStage.PRE) {
            mc.options.forwardKey.setPressed(!mc.options.sneakKey.isPressed()
                    && (!lockConfig.getValue() || (!mc.options.jumpKey.isPressed() && mc.player.isOnGround())));
        }
    }
}
