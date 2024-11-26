package org.nrnr.neverdies;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.Window;
import org.nrnr.neverdies.api.Identifiable;
import org.nrnr.neverdies.api.event.handler.EventBus;
import org.nrnr.neverdies.api.event.handler.EventHandler;
import org.nrnr.neverdies.api.file.ClientConfiguration;
import org.nrnr.neverdies.impl.gui.account.AccountSelectorScreen;
import org.nrnr.neverdies.impl.manager.client.AccountManager;
import org.nrnr.neverdies.init.Managers;
import org.nrnr.neverdies.init.Modules;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.nrnr.neverdies.RPC.startRPC;

public class Neverdies {
    // Client logger.
    // Client Event handler (aka Event bus) which handles event dispatching
    // and listening for client events.
    public static EventHandler EVENT_HANDLER;
    // Client configuration handler. This master saves/loads the client
    // configuration files which have been saved locally.
    public static ClientConfiguration CONFIG;
    // Client shutdown hooks which will run once when the MinecraftClient
    // game instance is shutdown.
    public static ShutdownHook SHUTDOWN;
    //
    public static Executor EXECUTOR;

    public static void init() {


        EXECUTOR = Executors.newFixedThreadPool(1);
        EVENT_HANDLER = new EventBus();
        Managers.init();
        Modules.init();
        CONFIG = new ClientConfiguration();
        Managers.postInit();
        SHUTDOWN = new ShutdownHook();
        Runtime.getRuntime().addShutdownHook(SHUTDOWN);
        startRPC();
        CONFIG.loadClient();

    }



    public static void info(String message) {
    }

    /**
     * @param message
     * @param params
     */
    public static void info(String message, Object... params) {
    }

    public static void info(Identifiable feature, String message) {
    }

    /**
     * @param feature
     * @param message
     * @param params
     */
    public static void info(Identifiable feature, String message,
                            Object... params) {
    }


    public static void error(String message) {
        ;
    }

    /**
     * @param message
     */
    public static void error(String message, Object... params) {
        ;
    }


    public static void error(Identifiable feature, String message) {
    }

    /**
     * @param feature
     * @param message
     * @param params
     */
    public static void error(Identifiable feature, String message,
                             Object... params) {
    }
}
