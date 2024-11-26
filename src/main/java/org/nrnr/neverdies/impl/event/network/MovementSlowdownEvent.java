package org.nrnr.neverdies.impl.event.network;

import net.minecraft.client.input.Input;
import org.nrnr.neverdies.api.event.Event;

/**
 * @author chronos
 * @since 1.0
 */
public class MovementSlowdownEvent extends Event {
    //
    public final Input input;

    /**
     * @param input
     */
    public MovementSlowdownEvent(Input input) {
        this.input = input;
    }

    /**
     * @return
     */
    public Input getInput() {
        return input;
    }
}
