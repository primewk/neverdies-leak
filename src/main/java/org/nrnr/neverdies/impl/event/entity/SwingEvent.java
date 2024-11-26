package org.nrnr.neverdies.impl.event.entity;

import net.minecraft.util.Hand;
import org.nrnr.neverdies.api.event.Cancelable;
import org.nrnr.neverdies.api.event.Event;

@Cancelable
public class SwingEvent extends Event {
    private final Hand hand;

    public SwingEvent(Hand hand) {
        this.hand = hand;
    }

    public Hand getHand() {
        return hand;
    }
}
