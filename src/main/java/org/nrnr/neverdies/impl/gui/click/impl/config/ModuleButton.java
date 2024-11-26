package org.nrnr.neverdies.impl.gui.click.impl.config;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.MacroConfig;
import org.nrnr.neverdies.api.macro.Macro;
import org.nrnr.neverdies.api.module.Module;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.api.render.RenderManager;
import org.nrnr.neverdies.impl.gui.click.component.Button;
import org.nrnr.neverdies.impl.gui.click.impl.config.setting.*;
import org.nrnr.neverdies.impl.module.render.BurrowEspModule;
import org.nrnr.neverdies.init.Modules;
import org.nrnr.neverdies.util.KeyboardUtil;
import org.nrnr.neverdies.util.render.animation.Animation;
import org.nrnr.neverdies.util.render.animation.Easing;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author chronos
 * @see Module
 * @see CategoryFrame
 * @since 1.0
 */
public class ModuleButton extends Button {
    private final Module module;
    //
    //public String  = "+";

    private final List<ConfigButton<?>> configComponents =
            new CopyOnWriteArrayList<>();
    //
    private float off;
    //
    private boolean open;
    private final Animation settingsAnimation = new Animation(false, 200, Easing.LINEAR);

    /**
     * @param module
     * @param frame
     * @param x
     * @param y
     */
    @SuppressWarnings("unchecked")
    public ModuleButton(Module module, CategoryFrame frame, float x, float y) {
        super(frame, x, y, 103.0f, 16.0f);
        this.module = module;
        for (Config<?> config : module.getConfigs()) {
            if (config.getName().equalsIgnoreCase("Enabled")) {
                continue;
            }
            if (config.getValue() instanceof Boolean) {
                configComponents.add(new CheckboxButton(frame, this,
                        (Config<Boolean>) config, x, y));
            } else if (config.getValue() instanceof Double) {
                configComponents.add(new SliderButton<>(frame, this,
                        (Config<Double>) config, x, y));
            } else if (config.getValue() instanceof Float) {
                configComponents.add(new SliderButton<>(frame, this,
                        (Config<Float>) config, x, y));
            } else if (config.getValue() instanceof Integer) {
                configComponents.add(new SliderButton<>(frame, this,
                        (Config<Integer>) config, x, y));
            } else if (config.getValue() instanceof Enum<?>) {
                configComponents.add(new DropdownButton(frame, this,
                        (Config<Enum<?>>) config, x, y));
            } else if (config.getValue() instanceof String) {
                configComponents.add(new TextButton(frame, this,
                        (Config<String>) config, x, y));
            } else if (config.getValue() instanceof Macro) {
                configComponents.add(new BindButton(frame, this,
                        (Config<Macro>) config, x, y));
            } else if (config.getValue() instanceof Color) {
                configComponents.add(new ColorButton(frame, this,
                        (Config<Color>) config, x, y));
            }
        }
        open = false;
    }

    /**
     * @param context
     * @param mouseX
     * @param mouseY
     * @param delta
     */
    @Override
    public void render(DrawContext context, float mouseX, float mouseY, float delta) {
        render(context, x, y, mouseX, mouseY, delta);
    }

    /**
     * @param context
     * @param mouseX
     * @param mouseY
     * @param delta
     */

    public float publicx, publicy;

    public void render(DrawContext context, float ix, float iy, float mouseX,
                       float mouseY, float delta) {
        x = ix;
        y = iy;
        float scaledTime = 1.0f;
        boolean fill = !(module instanceof ToggleModule t) || (scaledTime = (float) t.getAnimation().getFactor()) > 0.01f;
        scaledTime *= 1.7f;
        if (module.getName().equalsIgnoreCase("ClickGui")) {
            scaledTime = 1.7f;
        }

        rectGradient(context, fill ? Modules.CLICK_GUI.getColor(scaledTime) : 0x555555, fill ? Modules.CLICK_GUI.getColor1(scaledTime) : 0x555555);

        publicx = ix+2;
        publicy = iy+3.5f;

        RenderManager.renderText(context, module.getName(), ix + 2, iy + 3.5f, scaledTime > 0.99f ? -1 : 0xaaaaaa);

        //render icon here

        String config;
        Identifier iconIdentifier = new Identifier("neverdies", "textures/gear.png");
      //  context.drawTexture(iconIdentifier, (int) (ix + 91.5), (int) (iy + 3.5), 32, 32, 32, 32);


        String inputText = Modules.CLICK_GUI.inputText;
        ArrayList<String> modulesArray = Modules.CLICK_GUI.matchingModuleNames;


        int outlineColor = 0xFF000000;
        final float outlineThickness = 1.0f;

        if (Modules.COLORS.getSyncConfig()) {
            outlineColor = Modules.COLORS.getRGB();
        }
        else{
            outlineColor = Modules.COLORS.getOutlineColor().getRGB();

        }
        //drawRoundedRect(context, (int) ix, (int) iy, (int) width, (int) height, outlineColor);

        context.drawBorder((int) ix, (int) iy, (int) width, (int) height, outlineColor);

        if (settingsAnimation.getFactor() > 0.01f) {
            off = y + height + 1.0f;
            float fheight = 0.0f;
            for (ConfigButton<?> configButton : configComponents) {
                if (!configButton.getConfig().isVisible()) {
                    continue;
                }
                fheight += configButton.getHeight();
                if (configButton instanceof ColorButton colorPicker && colorPicker.getScaledTime() > 0.01f) {
                    fheight += colorPicker.getPickerHeight() * colorPicker.getScaledTime() * getScaledTime();
                }
            }

            enableScissor((int) x, (int) (off - 1.0f), (int) (x + width), (int) (off + 2.0f + (fheight * settingsAnimation.getFactor())));
            for (ConfigButton<?> configButton : configComponents) {
                if (!configButton.getConfig().isVisible()) {
                    continue;
                }
                // Run draw event
                configButton.render(context, ix + 2.0f, off, mouseX, mouseY, delta);
                ((CategoryFrame) frame).offset((float) (configButton.getHeight() * settingsAnimation.getFactor()));
                off += configButton.getHeight();
            }
            if (fill) {
                fill(context, ix, y + height, 1.0f, off - (y + height) + 1.0f, Modules.CLICK_GUI.getColor1(scaledTime));
                fill(context, ix + width - 1.0f, y + height, 1.0f, off - (y + height) + 1.0f, Modules.CLICK_GUI.getColor(scaledTime));
                fillGradient(context, ix, off + 1.0f, ix + width, off + 2.0f, Modules.CLICK_GUI.getColor(scaledTime), Modules.CLICK_GUI.getColor1(scaledTime));
            }
            disableScissor();
            ((CategoryFrame) frame).offset((float) (3.0f * settingsAnimation.getFactor()));
        }



    }


    public void renderDescription(DrawContext context, float x, float y, float width, float mouseX, float mouseY) {
            float textWidth = RenderManager.textWidth(module.getDescription());
            float bgX = mouseX - 1.0f;
            float bgY = mouseY - 12;
            float bgWidth = textWidth + 2.0f;
            float bgHeight = 12.0f;
            Color bgColor = Modules.COLORS.getColor(75);
            fill(context, bgX, bgY, bgWidth, bgHeight, bgColor.getRGB());
            RenderManager.renderText(context, module.getDescription(), mouseX, mouseY - 10, -1);
    }








    /**
     * @param mouseX
     * @param mouseY
     * @param button
     */
    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isWithin(mouseX, mouseY)) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && module instanceof ToggleModule t) {
                t.toggle();
                // ToggleGuiEvent toggleGuiEvent = new ToggleGuiEvent(t);
                // Caspian.EVENT_HANDLER.dispatch(toggleGuiEvent);
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                open = !open;
                settingsAnimation.setState(open);
            }
        }
        if (open) {
            for (ConfigButton<?> component : configComponents) {
                component.mouseClicked(mouseX, mouseY, button);
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
        if (open) {
            for (ConfigButton<?> component : configComponents) {
                component.mouseReleased(mouseX, mouseY, button);
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
        if (open) {
            for (ConfigButton<?> component : configComponents) {
                component.keyPressed(keyCode, scanCode, modifiers);
            }
        }
    }

    /**
     * @param in
     */
    public void offset(float in) {
        off += in;
    }

    /**
     * @return
     */
    public boolean isOpen() {
        return open;
    }

    public float getScaledTime() {
        return (float) settingsAnimation.getFactor();
    }

    /**
     * @return
     */
    public Module getModule() {
        return module;
    }


    /**
     * @return
     */
    public List<ConfigButton<?>> getConfigButtons() {
        return configComponents;
    }
}
