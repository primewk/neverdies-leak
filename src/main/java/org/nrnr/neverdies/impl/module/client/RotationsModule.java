package org.nrnr.neverdies.impl.module.client;

import org.lwjgl.glfw.GLFW;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.config.setting.NumberConfig;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ConcurrentModule;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.impl.command.PrefixCommand;
import org.nrnr.neverdies.impl.event.network.GameJoinEvent;
import org.nrnr.neverdies.init.Managers;
import org.nrnr.neverdies.init.Modules;
import org.nrnr.neverdies.util.KeyboardUtil;
import org.nrnr.neverdies.util.chat.ChatUtil;

/**
 * @author chronos
 * @since 1.0
 */
public class RotationsModule extends ConcurrentModule {
    //
    Config<Float> preserveTicksConfig = new NumberConfig<>("Limit Rotations", "Time to preserve rotations after reaching the target rotations", 0.0f, 10.0f, 20.0f);
    Config<Boolean> movementFixConfig = new BooleanConfig("Strict Direction(beta)", "Fixes movement on Grim when rotating", false);
    //
    private float prevYaw;

    /**
     *
     */
    public RotationsModule() {
        super("Rotations", "Manages client rotations",
                ModuleCategory.CLIENT);
    }

    @EventListener
    public void onGameJoin(GameJoinEvent event) {
        ChatUtil.clientSendMessageRaw("Welcome to Neverdies by Chronos, Mortex, and LachCrafter");
        ChatUtil.clientSendMessageRaw("The current ClickGUI bind is " + getKeyName(Modules.CLICK_GUI.getKeybinding().getKeycode()));
        ChatUtil.clientSendMessageRaw("The current Prefix is " + Managers.COMMAND.getPrefix());
    }

    public String getKeyName(int keycode) {
        if (keycode != GLFW.GLFW_KEY_UNKNOWN) {
            final String name = KeyboardUtil.getKeyName(keycode);
            return name != null ? name.toUpperCase() : "NONE";
        }
        return "NONE";
    }

    public boolean getMovementFix() {
        return movementFixConfig.getValue();
    }

    /**
     * @return
     */
    public float getPreserveTicks() {
        return preserveTicksConfig.getValue();
    }
}
