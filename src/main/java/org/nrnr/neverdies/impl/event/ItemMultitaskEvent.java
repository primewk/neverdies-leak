package org.nrnr.neverdies.impl.event;

import org.nrnr.neverdies.api.event.Cancelable;
import org.nrnr.neverdies.api.event.Event;
import org.nrnr.neverdies.mixin.MixinMinecraftClient;

/**
 * Allows mining and eating at the same time
 *
 * @see MixinMinecraftClient
 */
@Cancelable
public class ItemMultitaskEvent extends Event {

}
