package org.nrnr.neverdies.impl.event.text;

import org.nrnr.neverdies.api.event.Cancelable;
import org.nrnr.neverdies.api.event.Event;
import org.nrnr.neverdies.mixin.text.MixinTextVisitFactory;

/**
 * @see MixinTextVisitFactory
 */
@Cancelable
public class TextVisitEvent extends Event {
    //
    private String text;

    /**
     * @param text
     */
    public TextVisitEvent(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
