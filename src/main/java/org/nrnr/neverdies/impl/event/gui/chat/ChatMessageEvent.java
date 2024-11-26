package org.nrnr.neverdies.impl.event.gui.chat;

import net.minecraft.util.StringHelper;
import org.nrnr.neverdies.api.event.Cancelable;
import org.nrnr.neverdies.api.event.Event;
import org.apache.commons.lang3.StringUtils;

/**
 * @author chronos
 * @since 1.0
 */
@Cancelable
public class ChatMessageEvent extends Event {
    //
    private final String message;

    public ChatMessageEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return normalize(message);
    }

    /**
     * @return the {@code message} normalized by trimming it and then
     * normalizing spaces
     */
    private String normalize(String chatText) {
        return StringHelper.truncateChat(StringUtils.normalizeSpace(chatText.trim()));
    }

    @Cancelable
    public static class Client extends ChatMessageEvent {
        public Client(String message) {
            super(message);
        }
    }

    @Cancelable
    public static class Server extends ChatMessageEvent {
        public Server(String message) {
            super(message);
        }
    }
}
