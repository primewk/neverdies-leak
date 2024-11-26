package org.nrnr.neverdies.impl.event.gui.hud;

import org.nrnr.neverdies.api.event.Cancelable;
import org.nrnr.neverdies.api.event.Event;

@Cancelable
public class PlayerListColumnsEvent extends Event {
    private int tabHeight;

    public void setTabHeight(int tabHeight) {
        this.tabHeight = tabHeight;
    }

    public int getTabHeight() {
        return tabHeight;
    }
}
