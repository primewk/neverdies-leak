package org.nrnr.neverdies.impl.event.gui.hud;

import org.nrnr.neverdies.api.event.Cancelable;
import org.nrnr.neverdies.api.event.Event;

@Cancelable
public class PlayerListEvent extends Event {

    private int size;

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
