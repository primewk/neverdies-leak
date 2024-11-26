package org.nrnr.neverdies.impl.module.render;

import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.config.setting.ColorConfig;
import org.nrnr.neverdies.api.config.setting.NumberConfig;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.api.render.RenderManager;
import org.nrnr.neverdies.impl.event.render.RenderWorldEvent;
import org.nrnr.neverdies.impl.manager.combat.hole.Hole;
import org.nrnr.neverdies.impl.manager.combat.hole.HoleType;
import org.nrnr.neverdies.init.Managers;

import java.awt.*;

/**
 * @author chronos
 * @since 1.0
 */
public class HoleESPModule extends ToggleModule {
    //
    Config<Float> rangeConfig = new NumberConfig<>("Range", "Range to display holes", 3.0f, 10.0f, 25.0f);
    Config<Float> heightConfig = new NumberConfig<>("Size", "Render height of holes", -1.0f, 1.00f, 1.0f);
    Config<Boolean> obsidianCheckConfig = new BooleanConfig("Obsidian", "Displays obsidian holes", true);
    Config<Boolean> obsidianBedrockConfig = new BooleanConfig("Obsidian-Bedrock", "Displays mixed obsidian and bedrock holes", true);
    Config<Boolean> doubleConfig = new BooleanConfig("Double", "Displays double holes where the player can stand in the middle of two blocks to block explosion damage", true);
    Config<Boolean> quadConfig = new BooleanConfig("Quad", "Displays quad holes where the player can stand in the middle of four blocks to block explosion damage", false);
    Config<Boolean> voidConfig = new BooleanConfig("Void", "Displays void holes in the world", false);
    Config<Boolean> fadeConfig = new BooleanConfig("Fade", "Fades the opacity of holes based on distance", false);
    Config<Color> obsidianConfig = new ColorConfig("ObsidianColor", "The color for rendering obsidian holes", new Color(255, 0, 0, 100), () -> obsidianCheckConfig.getValue());
    Config<Color> mixedConfig = new ColorConfig("Obsidian-BedrockColor", "The color for rendering mixed holes", new Color(255, 255, 0, 100), () -> obsidianBedrockConfig.getValue());
    Config<Color> bedrockConfig = new ColorConfig("BedrockColor", "The color for rendering bedrock holes", new Color(0, 255, 0, 100));
    Config<Color> voidColorConfig = new ColorConfig("VoidColor", "The color for rendering bedrock holes", new Color(255, 0, 0, 160), () -> voidConfig.getValue());

    public HoleESPModule() {
        super("HoleESP", "Displays nearby blast resistant holes", ModuleCategory.RENDER);
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event) {
        if (mc.player == null) {
            return;
        }
        for (Hole hole : Managers.HOLE.getHoles()) {
            if ((hole.isDoubleX() || hole.isDoubleZ()) && !doubleConfig.getValue()
                    || hole.isQuad() && !quadConfig.getValue()
                    || hole.getSafety() == HoleType.VOID && !voidConfig.getValue()
                    || hole.getSafety() == HoleType.OBSIDIAN && !obsidianCheckConfig.getValue()
                    || hole.getSafety() == HoleType.OBSIDIAN_BEDROCK && !obsidianBedrockConfig.getValue()) {
                continue;
            }
            double dist = hole.squaredDistanceTo(mc.player);
            if (dist > ((NumberConfig) rangeConfig).getValueSq()) {
                continue;
            }
            double x = hole.getX();
            double y = hole.getY();
            double z = hole.getZ();
            Box render = null;
            if (hole.getSafety() == HoleType.VOID) {
                render = new Box(x, y, z, x + 1.0, y + 1.0, z + 1.0);
            } else if (hole.isDoubleX()) {
                render = new Box(x, y, z, x + 2.0,
                        y + heightConfig.getValue(), z + 1.0);
            } else if (hole.isDoubleZ()) {
                render = new Box(x, y, z, x + 1.0,
                        y + heightConfig.getValue(), z + 2.0);
            } else if (hole.isQuad()) {
                render = new Box(x, y, z, x + 2.0,
                        y + heightConfig.getValue(), z + 2.0);
            } else if (hole.isStandard()) {
                render = new Box(x, y, z, x + 1.0,
                        y + heightConfig.getValue(), z + 1.0);
            }
            if (render == null) {
                return;
            }
            double alpha = 1.0;
            if (fadeConfig.getValue()) {
                double fadeRange = rangeConfig.getValue() - 1.0;
                double fadeRangeSq = fadeRange * fadeRange;
                alpha = (fadeRangeSq + 9.0 - mc.player.squaredDistanceTo(hole.getX(),
                        hole.getY(), hole.getZ())) / fadeRangeSq;
                alpha = MathHelper.clamp(alpha, 0.0, 1.0);
            }
            RenderManager.renderBox(event.getMatrices(), render, getHoleColor(hole.getSafety(), alpha));
            RenderManager.renderBoundingBox(event.getMatrices(), render, 1.5f, getHoleColor(hole.getSafety(), (int) (alpha * 145.0f)));
        }
    }

    private int getHoleColor(HoleType holeType, double alpha) {
        ColorConfig obsidian = ((ColorConfig) obsidianConfig);
        ColorConfig mixed = ((ColorConfig) mixedConfig);
        ColorConfig bedrock = ((ColorConfig) bedrockConfig);
        ColorConfig voidColor = ((ColorConfig) voidColorConfig);
        return switch (holeType) {
            case OBSIDIAN -> obsidian.getRgb((int) (obsidian.getAlpha() * alpha));
            case OBSIDIAN_BEDROCK -> mixed.getRgb((int) (mixed.getAlpha() * alpha));
            case BEDROCK -> bedrock.getRgb((int) (bedrock.getAlpha() * alpha));
            case VOID -> voidColor.getRgb((int) (voidColor.getAlpha() * alpha));
        };
    }

    private int getHoleColor(HoleType holeType, int alpha) {
        return switch (holeType) {
            case OBSIDIAN -> ((ColorConfig) obsidianConfig).getRgb(alpha);
            case OBSIDIAN_BEDROCK -> ((ColorConfig) mixedConfig).getRgb(alpha);
            case BEDROCK -> ((ColorConfig) bedrockConfig).getRgb(alpha);
            case VOID -> ((ColorConfig) voidColorConfig).getRgb(alpha);
        };
    }

    public double getRange() {
        return rangeConfig.getValue();
    }
}
