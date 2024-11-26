package org.nrnr.neverdies.api.event.handler;

import org.nrnr.neverdies.api.event.Event;
import org.nrnr.neverdies.api.event.listener.Listener;

/**
 * @author chronos
 * @see Event
 * @see EventBus
 * @since 1.0
 */
public interface EventHandler {
    /**
     * Subscribes a {@link Object} to the EventHandler
     *
     * @param obj The subscriber object
     */
    void subscribe(Object obj);

    /**
     * Unsubscribes the subscriber {@link Class}
     *
     * @param obj The subscriber object
     */
    void unsubscribe(Object obj);

    /**
     * Runs all active {@link Listener} for the param {@link Event}
     *
     * @param event The event to dispatch listeners
     * @return <tt>true</tt> if {@link Event#isCanceled()}
     */
    boolean dispatch(Event event);
}
