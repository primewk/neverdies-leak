package org.nrnr.neverdies.impl.module.misc;

import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.config.setting.NumberConfig;
import org.nrnr.neverdies.api.event.EventStage;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.impl.event.TickEvent;
import org.nrnr.neverdies.impl.event.render.TickCounterEvent;
import org.nrnr.neverdies.init.Managers;
import org.nrnr.neverdies.init.Modules;

import java.text.DecimalFormat;

/**
 * @author chronos
 * @since 1.0
 */
public class TimerModule extends ToggleModule {
    //
    Config<Float> ticksConfig = new NumberConfig<>("Ticks", "The game tick speed", 0.1f, 2.0f, 50.0f);
    Config<Boolean> tpsSyncConfig = new BooleanConfig("TPSSync", "Syncs game tick speed to server tick speed", false);
    //
    private float prevTimer = -1.0f;
    private float timer = 1.0f;

    /**
     *
     */
    public TimerModule() {
        super("Timer", "Changes the client tick speed", ModuleCategory.MISCELLANEOUS);
    }

    @Override
    public String getModuleData() {
        DecimalFormat decimal = new DecimalFormat("0.0#");
        return decimal.format(timer);
    }

    @Override
    public void toggle() {
        Modules.SPEED.setPrevTimer();
        if (Modules.SPEED.isUsingTimer()) {
            return;
        }
        super.toggle();
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() == EventStage.PRE) {
            if (Modules.SPEED.isUsingTimer()) {
                return;
            }
            if (tpsSyncConfig.getValue()) {
                timer = Math.max(Managers.TICK.getTpsCurrent() / 20.0f, 0.1f);
                return;
            }
            timer = ticksConfig.getValue();
        }
    }

    @EventListener
    public void onTickCounter(TickCounterEvent event) {
        if (timer != 1.0f) {
            event.cancel();
            event.setTicks(timer);
        }
    }

    /**
     * @return
     */
    public float getTimer() {
        return timer;
    }

    /**
     * @param timer
     */
    public void setTimer(float timer) {
        prevTimer = this.timer;
        this.timer = timer;
    }

    public void resetTimer() {
        if (prevTimer > 0.0f) {
            this.timer = prevTimer;
            prevTimer = -1.0f;
        }
    }
}
