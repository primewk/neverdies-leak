package org.nrnr.neverdies.impl.event.keyboard;

import net.minecraft.client.input.Input;
import org.nrnr.neverdies.api.event.Cancelable;
import org.nrnr.neverdies.api.event.Event;
import org.nrnr.neverdies.api.event.StageEvent;

@Cancelable
public class KeyboardTickEvent extends StageEvent {

    private final Input input;

    public KeyboardTickEvent(Input input) {
        this.input = input;
    }

    public Input getInput() {
        return input;
    }
}
