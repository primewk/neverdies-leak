package org.nrnr.neverdies.impl.module.client;

import net.minecraft.client.gui.hud.ChatHudLine;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.config.setting.EnumConfig;
import org.nrnr.neverdies.api.config.setting.NumberConfig;
import org.nrnr.neverdies.api.module.ConcurrentModule;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.util.render.animation.Easing;
import org.nrnr.neverdies.util.render.animation.TimeAnimation;

import java.util.HashMap;
import java.util.Map;

/**
 * @author chronos
 * @since 1.0
 */
public class ChatModule extends ConcurrentModule {
    //
    Config<Boolean> debugConfig = new BooleanConfig("ChatDebug", "Allows client debug messages to be printed in the chat", false);

    /**
     *
     */
    public ChatModule() {
        super("Chat", "Manages the client chat", ModuleCategory.CLIENT);
    }
}
