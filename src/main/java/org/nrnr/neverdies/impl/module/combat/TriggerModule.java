package org.nrnr.neverdies.impl.module.combat;

import net.minecraft.entity.Entity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.EnumConfig;
import org.nrnr.neverdies.api.config.setting.NumberConfig;
import org.nrnr.neverdies.api.event.EventStage;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.impl.event.TickEvent;
import org.nrnr.neverdies.impl.imixin.IMinecraftClient;
import org.nrnr.neverdies.init.Managers;
import org.nrnr.neverdies.mixin.accessor.AccessorMinecraftClient;
import org.nrnr.neverdies.util.math.timer.CacheTimer;
import org.nrnr.neverdies.util.math.timer.Timer;

/**
 * @author chronos
 * @since 1.0
 */
public class TriggerModule extends ToggleModule {

    //
    Config<TriggerMode> modeConfig = new EnumConfig<>("Mode", "The mode for activating the trigger bot", TriggerMode.MOUSE_BUTTON, TriggerMode.values());
    Config<Float> attackSpeedConfig = new NumberConfig<>("AttackSpeed", "The speed to attack entities", 0.1f, 8.0f, 20.0f);
    Config<Float> randomSpeedConfig = new NumberConfig<>("RandomSpeed", "The speed randomizer for attacks", 0.1f, 2.0f, 10.0f);
    //
    private final Timer triggerTimer = new CacheTimer();

    /**
     *
     */
    public TriggerModule() {
        super("Trigger", "Automatically attacks entities in the crosshair",
                ModuleCategory.COMBAT);
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() != EventStage.PRE) {
            return;
        }
        boolean buttonDown = switch (modeConfig.getValue()) {
            case MOUSE_BUTTON -> mc.mouse.wasLeftButtonClicked();
            case MOUSE_OVER -> {
                if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.ENTITY) {
                    yield false;
                }
                EntityHitResult entityHit = (EntityHitResult) mc.crosshairTarget;
                final Entity crosshairEntity = entityHit.getEntity();
                if (mc.player.isTeammate(crosshairEntity)
                        || Managers.SOCIAL.isFriend(crosshairEntity.getName())) {
                    yield false;
                }
                yield true;
            }
            case MOUSE_CLICK -> true;
        };
        double d = Math.random() * randomSpeedConfig.getValue() * 2.0 - randomSpeedConfig.getValue();
        if (buttonDown && triggerTimer.passed(1000.0 - Math.max(attackSpeedConfig.getValue() + d, 0.5) * 50.0)) {
            ((IMinecraftClient) mc).leftClick();
            ((AccessorMinecraftClient) mc).hookSetAttackCooldown(0);
            triggerTimer.reset();
        }
    }

    public enum TriggerMode {
        MOUSE_BUTTON,
        MOUSE_OVER,
        MOUSE_CLICK
    }
}
