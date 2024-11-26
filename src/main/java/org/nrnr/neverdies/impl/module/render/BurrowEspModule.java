package org.nrnr.neverdies.impl.module.render;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.config.setting.ColorConfig;
import org.nrnr.neverdies.api.config.setting.EnumConfig;
import org.nrnr.neverdies.api.config.setting.NumberConfig;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.RotationModule;
import org.nrnr.neverdies.api.render.RenderManager;
import org.nrnr.neverdies.impl.event.network.PlayerTickEvent;
import org.nrnr.neverdies.impl.event.render.RenderWorldEvent;
import org.nrnr.neverdies.impl.module.world.AutoMineModule;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BurrowEspModule extends RotationModule {

    Config<Float> enemyRangeConfig = new NumberConfig<>("EnemyRange", "Range to search for targets", 1.0f, 5.0f, 15.0f);
    Config<Boolean> selfConfig = new BooleanConfig("Self", "Render if you are burrowed", false);
    Config<Render> renderConfig = new EnumConfig<>("Render", "Render a Box or an Outline", Render.BOX, Render.values());
    Config<Color> colorConfig = new ColorConfig("Color", "Only white/purplish works for now", new Color(0, 0, 0, 50), true, false);
    Config<Float> lineWidth = new NumberConfig<>("Outline-Width", "The width of the outline", 0.1f, 1.0f, 2.5f, () -> renderConfig.getValue() == Render.OUTLINE | renderConfig.getValue() == Render.BOX);

    private final List<BlockPos> posList = new ArrayList<>();

    public BurrowEspModule() {
        super("BurrowEsp", "Renders burrow models through wall", ModuleCategory.RENDER);
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event) {
        PlayerEntity playerTarget = null;
        double minDistance = Float.MAX_VALUE;

        for (PlayerEntity entity : mc.world.getPlayers()) {
            if (entity == mc.player) continue;
            double dist = mc.player.distanceTo(entity);
            if (dist > enemyRangeConfig.getValue()) continue;
            if (dist < minDistance) {
                minDistance = dist;
                playerTarget = entity;
            }
        }

        if (playerTarget != null) {
            MinecraftClient mc = MinecraftClient.getInstance();
            ClientPlayerEntity player = mc.player;
            if (player == null || mc.world == null) return;

            posList.clear(); //clear

            for (PlayerEntity otherPlayer : mc.world.getPlayers()) {
                BlockPos blockPos = new BlockPos((int) Math.floor(otherPlayer.getX()), (int) Math.floor(otherPlayer.getY() + 0.2), (int) Math.floor(otherPlayer.getZ()));
                if ((mc.world.getBlockState(blockPos).getBlock() == Blocks.ENDER_CHEST || mc.world.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN)) {
                    if (!(blockPos.getSquaredDistance(player.getPos()) <= 1.5) || selfConfig.getValue()) {
                        posList.add(blockPos);
                    }
                }
            }
        }
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;

        if (player == null || mc.world == null) return;

        posList.removeIf(blockPos ->
                !mc.world.getBlockState(blockPos).isOf(Blocks.ENDER_CHEST) &&
                        !mc.world.getBlockState(blockPos).isOf(Blocks.OBSIDIAN)
        );

        for (BlockPos blockPos : posList) {
            PlayerEntity closestPlayer = null;
            double minDistance = enemyRangeConfig.getValue();

            for (PlayerEntity entity : mc.world.getPlayers()) {
                if (entity == player) continue;
                double dist = player.distanceTo(entity);
                if (dist < minDistance) {
                    minDistance = dist;
                    closestPlayer = entity;
                }
            }

            if (closestPlayer != null && renderConfig.getValue() == Render.OUTLINE) {
                RenderManager.renderBoundingBox(event.getMatrices(), blockPos, lineWidth.getValue(), colorConfig.getValue().getRGB());
            }
            else if (closestPlayer != null && renderConfig.getValue() == Render.BOX){
                RenderManager.renderBox(event.getMatrices(), blockPos, colorConfig.getValue().getRGB());
            }
            else if (closestPlayer != null && renderConfig.getValue() == Render.BOTH){
                RenderManager.renderBoundingBox(event.getMatrices(), blockPos, lineWidth.getValue(), colorConfig.getValue().getRGB());
                RenderManager.renderBox(event.getMatrices(), blockPos, colorConfig.getValue().getRGB());
            }
        }
    }

    public enum Render{
        BOX,
        OUTLINE,
        BOTH
    }

}

