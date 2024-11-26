package org.nrnr.neverdies.impl.manager.client;

import org.nrnr.neverdies.Neverdies;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.file.ConfigFile;
import org.nrnr.neverdies.api.macro.Macro;
import org.nrnr.neverdies.impl.event.MouseClickEvent;
import org.nrnr.neverdies.impl.event.keyboard.KeyboardInputEvent;
import org.nrnr.neverdies.util.Globals;
import org.lwjgl.glfw.GLFW;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author chronos
 * @see Macro
 * @since 1.0
 */
public class MacroManager implements Globals {
    // For handling macros
    //
    private final Set<Macro> macros = new HashSet<>();

    /**
     *
     */
    public MacroManager() {
        Neverdies.EVENT_HANDLER.subscribe(this);
    }

    /**
     * @param event
     */
    @EventListener
    public void onKeyboardInput(KeyboardInputEvent event) {
        if (mc.player == null || mc.world == null
                || mc.currentScreen != null) {
            return;
        }
        // module keybind impl
        //
        if (macros.isEmpty()) {
            return;
        }
        for (Macro macro : macros) {
            // Ignore mouse binds
            if (macro.getKeycode() >= 1000) continue;
            if ((event.getAction() == GLFW.GLFW_PRESS) && event.getKeycode() != GLFW.GLFW_KEY_UNKNOWN
                    && event.getKeycode() == macro.getKeycode()) {
                macro.runMacro();
            }
        }
    }

    @EventListener
    public void onMouseInput(MouseClickEvent event) {
        if (mc.player == null || mc.world == null
                || mc.currentScreen != null) {
            return;
        }
        // module keybind impl
        //
        if (macros.isEmpty()) {
            return;
        }
        for (Macro macro : macros) {
            // Ignore keyboard binds
            if (macro.getKeycode() < 1000) continue;
            // Mouse binds start at 1000 here
            if ((event.getAction() == GLFW.GLFW_PRESS) && event.getButton() != GLFW.GLFW_KEY_UNKNOWN
                    && event.getButton() + 1000 == macro.getKeycode()) {
                macro.runMacro();
            }
        }
    }

    /**
     * Loads custom macros from the
     * {@link ConfigFile} system
     */
    public void postInit() {
        // TODO
    }

    public void setMacro(Macro macro, int keycode) {
        Macro m1 = getMacro(m -> m.getId().equals(macro.getId()));
        if (m1 != null) {
            m1.setKeycode(keycode);
        }
    }

    /**
     * @param macros
     */
    public void register(Macro... macros) {
        for (Macro macro : macros) {
            register(macro);
        }
    }

    /**
     * @param macro
     */
    public void register(Macro macro) {
        macros.add(macro);
    }

    public Macro getMacro(Predicate<? super Macro> predicate) {
        return macros.stream().filter(predicate).findFirst().orElse(null);
    }

    /**
     * @return
     */
    public Collection<Macro> getMacros() {
        return macros;
    }
}
