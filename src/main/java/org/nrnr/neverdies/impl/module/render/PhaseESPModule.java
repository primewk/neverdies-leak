package org.nrnr.neverdies.impl.module.render;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.config.setting.ColorConfig;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.api.render.RenderManager;
import org.nrnr.neverdies.impl.event.render.RenderWorldEvent;
import org.nrnr.neverdies.util.world.BlastResistantBlocks;

import java.awt.*;

public class PhaseESPModule extends ToggleModule {

    Config<Boolean> safeConfig = new BooleanConfig("Safe", "Highlights safe phase blocks", false);
    Config<Color> unsafeConfig = new ColorConfig("UnsafeColor", "The color for rendering unsafe phase blocks", new Color(255, 0, 0), false, false);
    Config<Color> obsidianConfig = new ColorConfig("ObsidianColor", "The color for rendering obsidian phase blocks", new Color(255, 255, 0), false, false, () -> safeConfig.getValue());
    Config<Color> bedrockConfig = new ColorConfig("BedrockColor", "The color for rendering bedrock phase blocks", new Color(0, 255, 0), false, false, () -> safeConfig.getValue());

    public PhaseESPModule() {
        super("PhaseESP", "Displays safe phase blocks", ModuleCategory.RENDER);
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event) {
        if (mc.player == null || mc.world == null || !mc.player.isOnGround()) {
            return;
        }
        BlockPos playerPos = mc.player.getBlockPos();
        for (Direction direction : Direction.values()) {
            if (!direction.getAxis().isHorizontal()) {
                continue;
            }
            BlockPos blockPos = playerPos.offset(direction);
            if (mc.world.getBlockState(blockPos).isReplaceable()) {
                continue;
            }
            Vec3d pos = mc.player.getPos();
            BlockState state = mc.world.getBlockState(blockPos.down());
            Color color = null;
            if (state.isReplaceable()) {
                color = unsafeConfig.getValue();
            } else if (safeConfig.getValue()) {
                if (BlastResistantBlocks.isUnbreakable(state.getBlock())) {
                    color = bedrockConfig.getValue();
                } else {
                    color = obsidianConfig.getValue();
                }
            }
            if (color == null) {
                continue;
            }
            double x = blockPos.getX();
            double y = blockPos.getY();
            double z = blockPos.getZ();
            double dx = pos.getX() - playerPos.getX();
            double dz = pos.getZ() - playerPos.getZ();
            if (direction == Direction.EAST && dx >= 0.65) {
                RenderManager.drawLine(event.getMatrices(), x, y, z, x, y, z + 1.0, color.getRGB());
            } else if (direction == Direction.WEST && dx <= 0.35) {
                RenderManager.drawLine(event.getMatrices(), x + 1.0, y, z, x + 1.0, y, z + 1.0, color.getRGB());
            } else if (direction == Direction.SOUTH && dz >= 0.65) {
                RenderManager.drawLine(event.getMatrices(), x, y, z, x + 1.0, y, z, color.getRGB());
            } else if (direction == Direction.NORTH && dz <= 0.35) {
                RenderManager.drawLine(event.getMatrices(), x, y, z + 1.0, x + 1.0, y, z + 1.0, color.getRGB());
            }
        }
    }
}
