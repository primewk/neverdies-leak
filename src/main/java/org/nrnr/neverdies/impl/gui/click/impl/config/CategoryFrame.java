package org.nrnr.neverdies.impl.gui.click.impl.config;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.module.Module;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.render.RenderManager;
import org.nrnr.neverdies.impl.gui.click.ClickGuiScreen;
import org.nrnr.neverdies.impl.gui.click.component.Frame;
import org.nrnr.neverdies.impl.gui.click.impl.Snow;
import org.nrnr.neverdies.impl.gui.click.impl.config.setting.ColorButton;
import org.nrnr.neverdies.impl.gui.click.impl.config.setting.ConfigButton;
import org.nrnr.neverdies.impl.module.client.HUDModule;
import org.nrnr.neverdies.init.Managers;
import org.nrnr.neverdies.init.Modules;
import org.nrnr.neverdies.util.render.animation.Animation;
import org.nrnr.neverdies.util.render.animation.Easing;
import org.nrnr.neverdies.util.string.EnumFormatter;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

/**
 * Configuration {@link Frame} (aka the "ClickGui" frames) which
 * allows the user to configure a {@link Module}'s {@link Config} values.
 *
 * @author chronos
 * @see Frame
 * @see Module
 * @see Config
 * @since 1.0
 */
public class CategoryFrame extends Frame {
    //
    private final String name;
    private final ModuleCategory category;
    // private final Identifier categoryIcon;
    // module components
    private final List<ModuleButton> moduleButtons =
            new CopyOnWriteArrayList<>();
    // global module offset
    private float off, inner;
    private boolean open;
    private boolean drag;
    //
    private final Animation categoryAnimation = new Animation(false, 5, Easing.CUBIC_IN_OUT);

    /**
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public CategoryFrame(ModuleCategory category, float x, float y,
                         float width, float height) {
        super(x, y, width, height);
        this.category = category;
        this.name = EnumFormatter.formatEnum(category);
        for (Module module : Managers.MODULE.getModules()) {
            if (module.getCategory() == category) {
                moduleButtons.add(new ModuleButton(module, this, x, y));
            }
        }
        categoryAnimation.setState(true);
        open = true;

    }


    /**
     * @param category
     * @param x
     * @param y
     */
    public CategoryFrame(ModuleCategory category, float x, float y) {
        this(category, x, y, 105.0f, 18.0f);
    }

    /**
     * @param context
     * @param mouseX
     * @param mouseY
     * @param delta
     */

    public String inputText;
    @Override
    public void render(DrawContext context, float mouseX, float mouseY, float delta) {


        if (drag) {
            x += ClickGuiScreen.MOUSE_X - px;
            y += ClickGuiScreen.MOUSE_Y - py;
        }
        fheight = 2.0f;
        for (ModuleButton moduleButton : moduleButtons) {
            int outlineColor = 0xFF000000;
            final float outlineThickness = 1.0f;

            if (Modules.COLORS.getSyncConfig()) {
                outlineColor = Modules.COLORS.getRGB();
            }
            else{
                outlineColor = Modules.COLORS.getOutlineColor().getRGB();

            }
            //drawRoundedRect(context, (int) ix, (int) iy, (int) width, (int) height, outlineColor);

            context.drawBorder((int) x, (int) y, (int) width, (int) height, outlineColor);

            // account for button height
            fheight += moduleButton.getHeight() + 1.0f;
            if (moduleButton.getScaledTime() < 0.01f) {
                continue;
            }
            fheight += 3.0f * moduleButton.getScaledTime();
            for (ConfigButton<?> configButton : moduleButton.getConfigButtons()) {
                if (!configButton.getConfig().isVisible()) {
                    continue;
                }
                // config button height may vary
                fheight += configButton.getHeight() * moduleButton.getScaledTime();
                if (configButton instanceof ColorButton colorPicker && colorPicker.getScaledTime() > 0.01f) {
                    fheight += colorPicker.getPickerHeight() * colorPicker.getScaledTime() * moduleButton.getScaledTime();
                }
            }
        }


        if (y < -(fheight - 10)) {
            y = -(fheight - 10);
        }
        if (y > mc.getWindow().getHeight() - 10) {
            y = mc.getWindow().getHeight() - 10;
        }
        //alpha
        rect(context, Modules.CLICK_GUI.getColor(2f));
        RenderManager.renderText(context, name, x + 3.0f, y + 4.0f, -1);
        if (categoryAnimation.getFactor() > 0.01f) {
            enableScissor((int) x, (int) (y + height), (int) (x + width), (int) (y + height + fheight * categoryAnimation.getFactor()));
            fill(context, x, y + height, width, fheight, 0x77000000);
            off = y + height + 1.0f;
            inner = off;
            for (ModuleButton moduleButton : moduleButtons) {
                moduleButton.render(context, x + 1.0f, inner + 1.0f, mouseX, mouseY, delta);
                off += (float) ((moduleButton.getHeight() + 1.0f) * categoryAnimation.getFactor());
                inner += moduleButton.getHeight() + 1.0f;
            }
            disableScissor();

        }
        // update previous position
        px = ClickGuiScreen.MOUSE_X;
        py = ClickGuiScreen.MOUSE_Y;



        int boxWidth = mc.textRenderer.getWidth(inputText) + 10;
        int boxHeight = 15;
        assert mc.currentScreen != null;
        int x = mc.currentScreen.width/2, y = mc.currentScreen.height/2 + (mc.currentScreen.height/3);


        RenderManager.rect(new MatrixStack(), x, y, boxWidth, boxHeight, Modules.COLORS.getRGB(75));

        RenderManager.renderText(context, inputText, x + 5, y + 5, 0xFFFFFFFF);
    }



    /**
     * @param mouseX
     * @param mouseY
     * @param mouseButton
     */
    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT && isWithin(mouseX, mouseY)) {
            open = !open;
            categoryAnimation.setState(open);
        }
        if (open) {
            for (ModuleButton button : moduleButtons) {
                button.mouseClicked(mouseX, mouseY, mouseButton);
            }
        }
    }

    /**
     * @param mouseX
     * @param mouseY
     * @param mouseButton
     */
    @Override
    public void mouseReleased(double mouseX, double mouseY, int mouseButton) {
        super.mouseReleased(mouseX, mouseY, mouseButton);
        drag = false;
        if (open) {
            for (ModuleButton button : moduleButtons) {
                button.mouseReleased(mouseX, mouseY, mouseButton);
            }
        }
    }

    /**
     * @param keyCode
     * @param scanCode
     * @param modifiers
     */
    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        super.keyPressed(keyCode, scanCode, modifiers);
        if (open) {
            for (ModuleButton button : moduleButtons) {
                button.keyPressed(keyCode, scanCode, modifiers);
            }
        }
    }

    /**
     * @param mx
     * @param my
     * @return
     */
    public boolean isWithinTotal(float mx, float my) {
        return isMouseOver(mx, my, x, y, width, getTotalHeight());
    }

    /**
     * Update global offset
     *
     * @param in The offset
     */
    public void offset(float in) {
        off += in;
        inner += in;
    }

    /**
     * @return
     */
    public ModuleCategory getCategory() {
        return category;
    }

    /**
     * Gets the total height of the frame
     *
     * @return The total height
     */
    public float getTotalHeight() {
        return height + fheight;
    }

    /**
     * @return
     */
    public List<ModuleButton> getModuleButtons() {
        return moduleButtons;
    }

    public void setDragging(boolean drag) {
        this.drag = drag;
    }

    public boolean isDragging() {
        return drag;
    }
}
