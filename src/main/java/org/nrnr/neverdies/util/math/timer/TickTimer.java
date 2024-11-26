package org.nrnr.neverdies.util.math.timer;

import org.nrnr.neverdies.Neverdies;
import org.nrnr.neverdies.api.event.EventStage;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.impl.event.TickEvent;

/**
 * TODO: Test the accuracy of ticks
 *
 * @author chronos
 * @see Timer
 * @since 1.0
 */
public class TickTimer implements Timer {
    //
    private long ticks;

    /**
     *
     */
    public TickTimer() {
        ticks = 0;
        Neverdies.EVENT_HANDLER.subscribe(this);
    }

    /**
     * @param event
     */
    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() == EventStage.PRE) {
            ++ticks;
        }
    }

    /**
     * Returns <tt>true</tt> if the time since the last reset has exceeded
     * the param time.
     *
     * @param time The param time
     * @return <tt>true</tt> if the time since the last reset has exceeded
     * the param time
     */
    @Override
    public boolean passed(Number time) {
        return ticks >= time.longValue();
    }

    /**
     *
     */
    @Override
    public void reset() {
        setElapsedTime(0);
    }

    /**
     * @return
     */
    @Override
    public long getElapsedTime() {
        return ticks;
    }

    /**
     * @param time
     */
    @Override
    public void setElapsedTime(Number time) {
        ticks = time.longValue();
    }
}
