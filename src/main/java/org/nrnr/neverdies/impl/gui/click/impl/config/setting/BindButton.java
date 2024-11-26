package org.nrnr.neverdies.impl.gui.click.impl.config.setting;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.MacroConfig;
import org.nrnr.neverdies.api.macro.Macro;
import org.nrnr.neverdies.api.render.RenderManager;
import org.nrnr.neverdies.impl.gui.click.impl.config.CategoryFrame;
import org.nrnr.neverdies.impl.gui.click.impl.config.ModuleButton;
import org.nrnr.neverdies.impl.module.client.ClickGuiModule;
import org.nrnr.neverdies.init.Modules;

import static org.lwjgl.glfw.GLFW.*;

/**
 * @author chronos
 * @since 1.0
 */
public class BindButton extends ConfigButton<Macro> {
    // Check for whether we are listening for an input
    private boolean listening;

    /**
     * @param frame
     * @param config
     * @param x
     * @param y
     */
    public BindButton(CategoryFrame frame, ModuleButton moduleButton, Config<Macro> config, float x, float y) {
        super(frame, moduleButton, config, x, y);
    }

    /**
     * @param context
     * @param ix
     * @param iy
     * @param mouseX
     * @param mouseY
     * @param delta
     */
    @Override
    public void render(DrawContext context, float ix, float iy, float mouseX,
                       float mouseY, float delta) {
        // If to allow the GUI to be exited by pressing ESC
        //context.drawBorder((int) x, (int) y, (int) width, (int) height, Modules.COLORS.getRGB());
        ClickGuiModule.CLICK_GUI_SCREEN.setCloseOnEscape(!listening);

        x = ix;
        y = iy;
        final Macro macro = config.getValue();
        String val = listening ? "..." : macro.getKeyName();
        rect(context, 0x00000000);
        RenderManager.renderText(context, config.getName() + Formatting.GRAY
                + " " + val, ix + 2.0f, iy + 4.0f, -1);
    }

    /**
     * @param mouseX
     * @param mouseY
     * @param button
     */
    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isWithin(mouseX, mouseY)) {
            if (button == GLFW_MOUSE_BUTTON_1) {
                listening = !listening;
            } else if (button == GLFW_MOUSE_BUTTON_2 && !listening) {
                // Reset the bind
                ((MacroConfig) config).setValue(GLFW_KEY_UNKNOWN);
            } else {
                if (listening) {
                    // Ignore Right click
                    if (button != GLFW_MOUSE_BUTTON_2) {
                        // Mouse bind
                        ((MacroConfig) config).setValue(1000 + button);
                    }
                    listening = false;
                }
            }
        }
    }

    /**
     * @param mouseX
     * @param mouseY
     * @param button
     */
    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {

    }

    /**
     * @param keyCode
     * @param scanCode
     * @param modifiers
     */
    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listening) {
            // unbind
            if (keyCode == GLFW_KEY_ESCAPE || keyCode == GLFW_KEY_BACKSPACE) {
                ((MacroConfig) config).setValue(GLFW_KEY_UNKNOWN);
            } else {
                ((MacroConfig) config).setValue(keyCode);
            }
            listening = false;
        }
    }

    public boolean isListening() {
        return listening;
    }

    public void setListening(boolean listening) {
        this.listening = listening;
    }
}
