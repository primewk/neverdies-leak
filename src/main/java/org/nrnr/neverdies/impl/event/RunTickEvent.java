package org.nrnr.neverdies.impl.event;

import org.nrnr.neverdies.api.event.Event;
import org.nrnr.neverdies.mixin.MixinMinecraftClient;

/**
 * The main game loop event, this "tick" runs while the
 * {@link net.minecraft.client.MinecraftClient#running} var is <tt>true</tt>.
 *
 * @author chronos
 * @see MixinMinecraftClient
 * @since 1.0
 */
public class RunTickEvent extends Event {

}
