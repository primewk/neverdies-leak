package org.nrnr.neverdies.impl.module.movement;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.config.setting.ColorConfig;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.RotationModule;
import org.nrnr.neverdies.api.render.RenderManager;
import org.nrnr.neverdies.impl.event.entity.player.PlayerMoveEvent;
import org.nrnr.neverdies.impl.event.network.MovementPacketsEvent;
import org.nrnr.neverdies.impl.event.render.RenderWorldEvent;
import org.nrnr.neverdies.impl.manager.combat.hole.Hole;
import org.nrnr.neverdies.impl.manager.combat.hole.HoleManager;
import org.nrnr.neverdies.init.Managers;
import org.nrnr.neverdies.init.Modules;
import org.nrnr.neverdies.util.chat.ChatUtil;
import org.nrnr.neverdies.util.world.BlockUtil;

import java.awt.*;
import java.util.Objects;

public class HoleSnapModule extends RotationModule {

    private Config<Boolean> antiKickConfig = new BooleanConfig("AntiKick", "Prevents vanilla flight detection", false);
    private Config<Boolean> modeConfig = new BooleanConfig("AutoTravel", "Auto Travels to the Hole", true);
    private Config<Boolean> disableConfig = new BooleanConfig("AutoDisable", "Disables After Use", false);
    private Config<Boolean> lineConfig = new BooleanConfig("Render Line", "Line Renders", false);
    private Config<Color> colorConfig = new ColorConfig("Line Color", "The color of the line", new Color(255, 0, 0, 60), () -> lineConfig.getValue());
    private Config<Boolean> labelConfig = new BooleanConfig("Renders Text", "Text Renders", false);
    private float prevClientYaw;

    public HoleSnapModule() {
        super("HoleSnap", "Moves the player to the nearest hole", ModuleCategory.MOVEMENT);
    }

    @Override
    public void onEnable() {
        Hole nearestHole = getNearestHole();
        if (nearestHole != null) {
            if (disableConfig.getValue()) {
            }
        } else {
            ChatUtil.clientSendMessage("No holes found");
        }
    }



    private Hole getNearestHole() {
        Hole nearestHole = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Hole hole : Managers.HOLE.getHoles()) {
            double distance = mc.player.squaredDistanceTo(hole.getX(), hole.getY(), hole.getZ());
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestHole = hole;
            }
        }
        return nearestHole;
    }

    /*private void moveToHole(Hole hole) {
        double x = hole.getX() - mc.player.getX();
        double y = hole.getY() - mc.player.getY();
        double z = hole.getZ() - mc.player.getZ();

        double distance = Math.sqrt(x * x + y * y + z * z);
        if (distance > 0) {
            x /= distance;
            y /= distance;
            z /= distance;

            mc.player.setVelocity(x * 0.5, y * 0.5, z * 0.5);
        }
    }
        */
    @EventListener
    public void onMove(final PlayerMoveEvent event) {
        if (mc.player == null) return;

        BlockPos bp = BlockPos.ofFloored(mc.player.getPos());

        for (int i = 1; i < 5; i++) {
            if(getNearestHole() != null) continue;

            Vec3d center = new Vec3d(
                    Math.floor(mc.player.getX()) + 0.5,
                    mc.player.getY(),
                    Math.floor(mc.player.getZ()) + 0.5
            );
            if (center.distanceTo(mc.player.getPos()) < 0.15f) {
                event.setX(0);
                event.setZ(0);
                event.cancel();
            }

            break;
        }

        if (mc.player != null && mc.player.horizontalCollision && mc.player.isOnGround())
            mc.player.jump();

        if (modeConfig.getValue() && getNearestHole() != null) {
            final double newYaw = Math.cos(Math.toRadians((getNewYaw(getNearestHole().getCenter()) + 90.0f)));
            final double newPitch = Math.sin(Math.toRadians(getNewYaw(getNearestHole().getCenter()) + 90.0f));
            final double diffX = getNearestHole().getCenter().getX() - mc.player.getX();
            final double diffZ = getNearestHole().getCenter().getZ() - mc.player.getZ();
            final double x = 0.29 * newYaw;
            final double z = 0.29 * newPitch;

            event.setX(Math.abs(x) < Math.abs(diffX) ? x : diffX);
            event.setZ(Math.abs(z) < Math.abs(diffZ) ? z : diffZ);
            event.cancel();

        }

    }

    private float getNewYaw(@NotNull Vec3d pos) {
        if (mc.player == null)
            return 0;

        return mc.player.getYaw() + MathHelper.wrapDegrees((float) Math.toDegrees(Math.atan2(pos.getZ() - mc.player.getZ(), pos.getX() - mc.player.getX())) - mc.player.getYaw() - 90);
    }

    private void doYawModeLogic(boolean isPreEvent) {
        if (getNearestHole() == null || mc.player == null || modeConfig.getValue()) return;

        if (isPreEvent) {
            prevClientYaw = mc.player.getYaw();
            mc.player.setYaw(BlockUtil.calculateAngle(getNearestHole().getCenter())[0]);
        } else
            mc.player.setYaw(prevClientYaw);
    }


    @EventListener
    public void onRenderWorld(RenderWorldEvent event) {

        for (Hole hole : Managers.HOLE.getHoles()) {
            if (lineConfig.getValue()){
                RenderManager.renderLine(event.getMatrices(), mc.player.getX(), mc.player.getY(), mc.player.getZ(), hole.getCenter().getX(), hole.getCenter().getY(), hole.getCenter().getZ(), 15f, colorConfig.getValue().getRGB());
            }
            if (labelConfig.getValue()){
                RenderManager.renderSign(event.getMatrices(), "Hole", hole.getCenter().getX(), hole.getCenter().getY(), hole.getCenter().getZ());
            }
        }
    }
}
