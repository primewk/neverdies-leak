package org.nrnr.neverdies;

import org.nrnr.neverdies.api.file.ClientConfiguration;

import static org.nrnr.neverdies.RPC.stopRPC;

/**
 * @author chronos
 * @since 1.0
 */
public class ShutdownHook extends Thread {
    /**
     *
     */
    public ShutdownHook() {
        setName("Neverdies-ShutdownHook");
    }

    /**
     * This runs when the game is shutdown and saves the
     * {@link ClientConfiguration} files.
     *
     * @see ClientConfiguration#saveClient()
     */
    @Override
    public void run() {
        stopRPC();
        Neverdies.info("Saving configurations and shutting down!");
        Neverdies.CONFIG.saveClient();
    }
}
