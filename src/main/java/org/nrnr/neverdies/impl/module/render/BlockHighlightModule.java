package org.nrnr.neverdies.impl.module.render;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.config.setting.EnumConfig;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.api.render.BoxRender;
import org.nrnr.neverdies.api.render.RenderManager;
import org.nrnr.neverdies.impl.event.render.RenderBlockOutlineEvent;
import org.nrnr.neverdies.impl.event.render.RenderWorldEvent;
import org.nrnr.neverdies.init.Managers;
import org.nrnr.neverdies.init.Modules;

import java.text.DecimalFormat;

/**
 * @author chronos
 * @since 1.0
 */
public class BlockHighlightModule extends ToggleModule {

    Config<BoxRender> boxModeConfig = new EnumConfig<>("BoxMode", "Box rendering mode", BoxRender.OUTLINE, BoxRender.values());
    Config<Boolean> entitiesConfig = new BooleanConfig("Debug-Entities", "Highlights entity bounding boxes for debug purposes", false);
    private double distance;

    public BlockHighlightModule() {
        super("BlockHighlight", "Highlights the block the player is facing", ModuleCategory.RENDER);
    }

    @Override
    public String getModuleData() {
        DecimalFormat decimal = new DecimalFormat("0.0");
        return decimal.format(distance);
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event) {
        if (mc.world == null) {
            return;
        }
        Box render = null;
        final HitResult result = mc.crosshairTarget;
        if (result != null) {
            final Vec3d pos = Managers.POSITION.getEyePos();
            if (entitiesConfig.getValue()
                    && result.getType() == HitResult.Type.ENTITY) {
                final Entity entity = ((EntityHitResult) result).getEntity();
                render = entity.getBoundingBox();
                distance = pos.distanceTo(entity.getPos());
            } else if (result.getType() == HitResult.Type.BLOCK) {
                BlockPos hpos = ((BlockHitResult) result).getBlockPos();
                BlockState state = mc.world.getBlockState(hpos);
                VoxelShape outlineShape = state.getOutlineShape(mc.world, hpos);
                if (outlineShape.isEmpty()) {
                    return;
                }
                Box render1 = outlineShape.getBoundingBox();
                render = new Box(hpos.getX() + render1.minX, hpos.getY() + render1.minY,
                        hpos.getZ() + render1.minZ, hpos.getX() + render1.maxX,
                        hpos.getY() + render1.maxY, hpos.getZ() + render1.maxZ);
                distance = pos.distanceTo(hpos.toCenterPos());
            }
        }
        if (render != null) {
            switch (boxModeConfig.getValue()) {
                case FILL -> {
                    RenderManager.renderBox(event.getMatrices(), render,
                            Modules.COLORS.getRGB(60));
                    RenderManager.renderBoundingBox(event.getMatrices(),
                            render, 2.5f, Modules.COLORS.getRGB(145));
                }
                case OUTLINE -> RenderManager.renderBoundingBox(event.getMatrices(),
                        render, 2.5f, Modules.COLORS.getRGB(145));
            }
        }
    }

    @EventListener
    public void onRenderBlockOutline(RenderBlockOutlineEvent event) {
        event.cancel();
    }
}
