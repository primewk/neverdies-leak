package org.nrnr.neverdies.impl.event.gui.hud;

import net.minecraft.text.OrderedText;
import org.nrnr.neverdies.api.event.Cancelable;
import org.nrnr.neverdies.api.event.Event;

@Cancelable
public class ChatTextEvent extends Event {

    private OrderedText text;

    public ChatTextEvent(OrderedText text) {
        this.text = text;
    }

    public void setText(OrderedText text) {
        this.text = text;
    }

    public OrderedText getText() {
        return text;
    }
}
