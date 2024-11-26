package org.nrnr.neverdies.impl.gui.click;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.render.RenderManager;
import org.nrnr.neverdies.impl.event.keyboard.KeyboardInputEvent;
import org.nrnr.neverdies.impl.gui.click.component.ScissorStack;
import org.nrnr.neverdies.impl.gui.click.impl.Snow;
import org.nrnr.neverdies.impl.gui.click.impl.config.CategoryFrame;
import org.nrnr.neverdies.impl.gui.click.impl.config.ModuleButton;
import org.nrnr.neverdies.impl.gui.click.impl.config.setting.BindButton;
import org.nrnr.neverdies.impl.gui.click.impl.config.setting.ConfigButton;
import org.nrnr.neverdies.impl.module.client.ClickGuiModule;
import org.nrnr.neverdies.init.Modules;
import org.nrnr.neverdies.util.Globals;
import org.lwjgl.glfw.GLFW;
import org.nrnr.neverdies.util.KeyboardUtil;
import org.nrnr.neverdies.util.chat.ChatUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.lwjgl.glfw.GLFW.*;

/**
 * @author chronos
 * @see ClickGuiModule
 * @since 1.0
 */
public class ClickGuiScreen extends Screen implements Globals {
    // mouse position
    public String inputText = "";

    public static int MOUSE_X;
    public static int MOUSE_Y;
    // mouse states
    private ArrayList<Snow> _snowList = new ArrayList<Snow>();
    public static boolean MOUSE_RIGHT_CLICK;
    public static boolean MOUSE_RIGHT_HOLD;
    public static boolean MOUSE_LEFT_CLICK;
    public static boolean MOUSE_LEFT_HOLD;
    //
    public static final ScissorStack SCISSOR_STACK = new ScissorStack();
    private final List<CategoryFrame> frames = new CopyOnWriteArrayList<>();
    private final ClickGuiModule module;
    //
    private CategoryFrame focus;
    //
    private boolean closeOnEscape = true;

    /**
     *
     */
    public ClickGuiScreen(ClickGuiModule module) {
        super(Text.literal("ClickGui"));
        this.module = module;
        float x = 2.0f;
        for (ModuleCategory category : ModuleCategory.values()) {
            CategoryFrame frame = new CategoryFrame(category, x, 23.0f);
            frames.add(frame);
            x += frame.getWidth() + 2.0f;
        }
    }

    /**
     * @param context
     * @param mouseX
     * @param mouseY
     * @param delta
     */

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {


        for (CategoryFrame frame : frames) {
            if (frame.isWithinTotal(mouseX, mouseY)) {
                focus = frame;
            }
            if (frame.isWithin(mouseX, mouseY) && MOUSE_LEFT_HOLD && checkDragging()) {
                frame.setDragging(true);
            }
            frame.render(context, mouseX, mouseY, delta);
            float scale = module.getScale();
            if (scale != 1.0f) {
                frame.setDimensions(frame.getWidth() * scale,
                        frame.getHeight() * scale);
                for (ModuleButton button : frame.getModuleButtons()) {
                    button.setDimensions(button.getWidth() * scale,
                            button.getHeight() * scale);
                    for (ConfigButton<?> component : button.getConfigButtons()) {
                        if (component instanceof BindButton bindButton && bindButton.isListening() && !button.isOpen()) {
                            bindButton.setListening(false);
                            setCloseOnEscape(true);
                        }

                        component.setDimensions(component.getWidth() * scale,
                                component.getHeight() * scale);
                    }
                }
            }


        }
        if (Modules.CLICK_GUI.renderDescription.getValue()){
            for (CategoryFrame frame : frames) {
                for (ModuleButton button : frame.getModuleButtons()) {
                    if (button.isWithin(mouseX, mouseY)) {
                        button.renderDescription(context, 0, 0, width, 0, 490);
                        break;
                    }
                }
            }
        }

        // update mouse state
        MOUSE_LEFT_CLICK = false;
        MOUSE_RIGHT_CLICK = false;
        // update mouse position
        MOUSE_X = mouseX;
        MOUSE_Y = mouseY;
        Random random = new Random();
        int maxSnowflakes = 225;

        for (int i = 0; i < maxSnowflakes; ++i) {
            for (int y = 0; y < 3; ++y) {
                // Check if we've already reached the maximum number of snowflakes
                if (_snowList.size() >= maxSnowflakes) {
                    break;
                }

                Snow snow = new Snow(25 * i, y * -50, 35, random.nextInt(2) + 1);
                _snowList.add(snow);
            }
        }

        if (!_snowList.isEmpty()) {
            _snowList.forEach(snow -> snow.Update(mc.getWindow(), context));
        }


    }


    /**
     * @param mouseX
     * @param mouseY
     * @param mouseButton
     * @return
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            MOUSE_LEFT_CLICK = true;
            MOUSE_LEFT_HOLD = true;
        } else if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            MOUSE_RIGHT_CLICK = true;
            MOUSE_RIGHT_HOLD = true;
        }
        for (CategoryFrame frame : frames) {
            frame.mouseClicked(mouseX, mouseY, mouseButton);
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    /**
     * @param mouseX the X coordinate of the mouse
     * @param mouseY the Y coordinate of the mouse
     * @param button the mouse button number
     * @return
     */
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            MOUSE_LEFT_HOLD = false;
        } else if (button == 1) {
            MOUSE_RIGHT_HOLD = false;
        }
        for (CategoryFrame frame : frames) {
            frame.mouseReleased(mouseX, mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    /**
     * @param mouseX the X coordinate of the mouse
     * @param mouseY the Y coordinate of the mouse
     * @return
     */
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (CategoryFrame frame : frames) {
            frame.setPos(frame.getX(), (float) (frame.getY() + verticalAmount * 10));
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }


    /**
     * @param keyCode
     * @param scanCode
     * @param modifiers
     * @return
     */

    /**
     * @return
     */
    @Override
    public boolean shouldPause() {
        return false;
    }

    /**
     *
     */
    @Override
    public void close() {
        module.disable();
        //
        MOUSE_LEFT_CLICK = false;
        MOUSE_LEFT_HOLD = false;
        MOUSE_RIGHT_CLICK = false;
        MOUSE_RIGHT_HOLD = false;
        super.close();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return closeOnEscape;
    }

    private boolean checkDragging() {
        for (CategoryFrame frame : frames) {
            if (frame.isDragging()) {
                return false;
            }
        }
        return true;
    }

    public void setCloseOnEscape(boolean closeOnEscape) {
        this.closeOnEscape = closeOnEscape;
    }
}
