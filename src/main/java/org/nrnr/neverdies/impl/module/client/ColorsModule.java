package org.nrnr.neverdies.impl.module.client;

import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.config.setting.ColorConfig;
import org.nrnr.neverdies.api.module.ConcurrentModule;
import org.nrnr.neverdies.api.module.ModuleCategory;

import java.awt.*;

/**
 * @author chronos
 * @since 1.0
 */
public class ColorsModule extends ConcurrentModule {
    //
    Config<Color> colorConfig = new ColorConfig("Color", "The primary client color", new Color(255, 255, 255), false, false);
    Config<Color> outlineColorConfig = new ColorConfig("Outline Color", "The outline color", new Color(0, 0, 0), false, false);
    Config<Boolean> syncConfig = new BooleanConfig("Sync Colors", "Sync outline and other", false);
    // Config<Color> color1Config = new ColorConfig("Accent-Color", "The accent client color", new Color());
    Config<Boolean> rainbowConfig = new BooleanConfig("Rainbow", "Renders rainbow colors for modules", false);

    public boolean getSyncConfig() {
        return syncConfig.getValue();
    }
    /**
     *
     */

    public ColorsModule() {
        super("Colors", "Client color scheme", ModuleCategory.CLIENT);
    }

    public Color getColor() {
        return colorConfig.getValue();
    }

    public Color getOutlineColor() {
        return outlineColorConfig.getValue();
    }


    public Color getColor(float alpha) {
        ColorConfig config = (ColorConfig) colorConfig;
        return new Color(config.getRed() / 255.0f, config.getGreen() / 255.0f, config.getBlue() / 255.0f, alpha);
    }

    public Color getColor(int alpha) {
        ColorConfig config = (ColorConfig) colorConfig;
        return new Color(config.getRed(), config.getGreen(), config.getBlue(), alpha);
    }

    public Color getOutlineColor(int alpha) {
        ColorConfig config = (ColorConfig) outlineColorConfig;
        return new Color(config.getRed(), config.getGreen(), config.getBlue(), alpha);
    }

    public Integer getRGB() {
        return getColor().getRGB();
    }

    public int getRGB(int a) {
        return getColor(a).getRGB();
    }


    public int getOutlineRGB(int a) {
        return getOutlineColor(a).getRGB();
    }


    public void onTick(){
        //ChatUtil.clientSendMessage("Welcome to Neverdies Client 1.20.4 by ChronosUser and Mortex8!");
        //ChatUtil.clientSendMessage("The Default Prefix is + and the Default ClickGui Bind is RSHIFT");

        
    }
}
