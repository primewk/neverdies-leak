package org.nrnr.neverdies.impl.event.config;

import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.event.StageEvent;

/**
 * @author chronos
 * @since 1.0
 */
public class ConfigUpdateEvent extends StageEvent {
    //
    private final Config<?> config;

    /**
     * @param config
     */
    public ConfigUpdateEvent(Config<?> config) {
        this.config = config;
    }

    /**
     * @return
     */
    public Config<?> getConfig() {
        return config;
    }
}
