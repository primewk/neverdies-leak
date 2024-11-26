package org.nrnr.neverdies.api.discord.callbacks;

import com.sun.jna.Callback;
import org.nrnr.neverdies.api.discord.DiscordUser;

public interface ReadyCallback extends Callback {
    void apply(final DiscordUser p0);
}
