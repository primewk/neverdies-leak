package org.nrnr.neverdies.impl.event.gui.chat;

import org.nrnr.neverdies.api.event.Cancelable;
import org.nrnr.neverdies.api.event.Event;

/**
 * @author chronos
 * @since 1.0
 */
@Cancelable
public class ChatKeyInputEvent extends Event {
    //
    private final int keycode;
    private String chatText;

    /**
     * @param keycode
     * @param chatText
     */
    public ChatKeyInputEvent(int keycode, String chatText) {
        this.keycode = keycode;
        this.chatText = chatText;
    }

    public int getKeycode() {
        return keycode;
    }

    public String getChatText() {
        return chatText;
    }

    public void setChatText(String chatText) {
        this.chatText = chatText;
    }
}
