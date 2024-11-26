package org.nrnr.neverdies.impl.module.movement;

import net.minecraft.entity.effect.StatusEffects;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.EnumConfig;
import org.nrnr.neverdies.api.event.EventStage;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.impl.event.TickEvent;
import org.nrnr.neverdies.impl.event.network.SprintCancelEvent;
import org.nrnr.neverdies.util.player.MovementUtil;
import org.nrnr.neverdies.util.string.EnumFormatter;

/**
 * @author chronos
 * @since 1.0
 */
public class SprintModule extends ToggleModule {
    //
    Config<SprintMode> modeConfig = new EnumConfig<>("Mode", "Sprinting mode. Rage allows for multi-directional sprinting.", SprintMode.LEGIT, SprintMode.values());

    /**
     *
     */
    public SprintModule() {
        super("Sprint", "Automatically sprints", ModuleCategory.MOVEMENT);
    }

    @Override
    public String getModuleData() {
        return EnumFormatter.formatEnum(modeConfig.getValue());
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() != EventStage.PRE) {
            return;
        }
        if (MovementUtil.isInputtingMovement()
                && !mc.player.isSneaking()
                && !mc.player.isRiding()
                && !mc.player.isTouchingWater()
                && !mc.player.isInLava()
                && !mc.player.isHoldingOntoLadder()
                && !mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
                && mc.player.getHungerManager().getFoodLevel() > 6.0F) {
            switch (modeConfig.getValue()) {
                case LEGIT -> {
                    if (mc.player.input.hasForwardMovement()
                            && (!mc.player.horizontalCollision
                            || mc.player.collidedSoftly)) {
                        mc.player.setSprinting(true);
                    }
                }
                case RAGE -> mc.player.setSprinting(true);
            }
        }
    }

    @EventListener
    public void onSprintCancel(SprintCancelEvent event) {
        if (MovementUtil.isInputtingMovement()
                && !mc.player.isSneaking()
                && !mc.player.isRiding()
                && !mc.player.isTouchingWater()
                && !mc.player.isInLava()
                && !mc.player.isHoldingOntoLadder()
                && !mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
                && mc.player.getHungerManager().getFoodLevel() > 6.0F
                && modeConfig.getValue() == SprintMode.RAGE) {
            event.cancel();
        }
    }

    public enum SprintMode {
        LEGIT,
        RAGE
    }
}
