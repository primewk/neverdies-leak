package org.nrnr.neverdies.impl.module.combat;


import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.config.setting.EnumConfig;
import org.nrnr.neverdies.api.config.setting.NumberConfig;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.RotationModule;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.api.render.Interpolation;
import org.nrnr.neverdies.api.render.RenderManager;
import org.nrnr.neverdies.impl.event.network.PlayerTickEvent;
import org.nrnr.neverdies.impl.event.render.RenderWorldEvent;
import org.nrnr.neverdies.init.Managers;
import org.nrnr.neverdies.init.Modules;
import org.nrnr.neverdies.util.chat.ChatUtil;
import org.nrnr.neverdies.util.math.timer.CacheTimer;
import org.nrnr.neverdies.util.math.timer.Timer;
import org.nrnr.neverdies.util.player.FindItemResult;
import org.nrnr.neverdies.util.player.InventoryUtil;
import org.nrnr.neverdies.util.player.PlayerUtil;
import org.nrnr.neverdies.util.player.RotationUtil;
import org.nrnr.neverdies.util.world.BlockUtil;
import org.nrnr.neverdies.util.world.CardinalDirection;
import org.nrnr.neverdies.util.world.EntityUtil;
import org.nrnr.neverdies.util.world.ExplosionUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
  created by  chronos
 */

public class AutoAnchorModule extends RotationModule {

    Config<Boolean> multitaskConfig = new BooleanConfig("Multitask", "Allows attacking while using items", true);
    Config<Boolean> whileMiningConfig = new BooleanConfig("WhileMining", "Allows attacking while mining blocks", true);
    Config<Float> targetRangeConfig = new NumberConfig<>("EnemyRange", "Range to search for potential enemies", 1.0f, 10.0f, 13.0f);
    Config<Float> maxSelfDamage = new NumberConfig<>("Self Damage", "Amount of Self Damage", 1.0f, 10.0f, 30.0f);
    Config<Float> minDamage = new NumberConfig<>("Min Damage", "Amount of Min Damage", 1.0f, 5.0f, 30.0f);
    Config<Integer> delay = new NumberConfig<>("Delay", "Place Delay", 0, 1, 10);
    Config<Boolean> placeConfig = new BooleanConfig("Place", "Place Bed", true);
    Config<Boolean> breakConfig = new BooleanConfig("Break", "Break/Attack Bed", false);
    Config<Boolean> rotateConfig = new BooleanConfig("Rotate", "Simple Rotate", true);
    Config<Boolean> antiSuicideConfig = new BooleanConfig("Anti-Suicide", "Don't kill yourself ig.", false);
    Config<Boolean> strictDirectionConfig = new BooleanConfig("StrictDirection", "Interacts with only visible directions when placing crystals", true);
    public AutoAnchorModule() {
        super("AutoAnchor", "Automatically places and explodes anchors",
                ModuleCategory.COMBAT);
    }


    private BlockPos renderpos;
    private int timer;
    private BlockPos placePos, breakPos;

    @EventListener
    public void onEnable(){
    }

    @EventListener
    public void onPlayerTick(final PlayerTickEvent event) {
        if (!mc.world.getDimension().respawnAnchorWorks()) {
            ChatUtil.clientSendMessage("You can't blow up anchors in this dimension, disabling.");
            toggle();
            return;
        }
        PlayerEntity playerTarget = null;
        double minDistance = Float.MAX_VALUE;
        for (PlayerEntity entity : mc.world.getPlayers()) {
            if (entity == mc.player || Managers.SOCIAL.isFriend(entity.getName())) {
                continue;
            }
            double dist = mc.player.distanceTo(entity);
            if (dist > targetRangeConfig.getValue()) {
                continue;
            }
            if (dist < minDistance) {
                minDistance = dist;
                playerTarget = entity;
            }
            double highest = -1;
        }

        if (!(playerTarget == null)){
            placePos = findPlace(playerTarget);
        }
        if (!(placePos == null)){
            renderpos = placePos;
            ChatUtil.clientSendMessage(String.valueOf(renderpos));
        }
    }


    private CardinalDirection direction;

    private BlockPos findPlace(PlayerEntity playerTarget) {

        ArrayList<BlockPos> anchorPos = new ArrayList<>();
        BlockPos playerPos = BlockPos.ofFloored(playerTarget.getPos());
        double minDistance = Float.MAX_VALUE;


            BlockPos[] offsets = {
                    new BlockPos(1, -1, 0),
                    new BlockPos(-1, -1, 0),
                    new BlockPos(0, -1, 1),
                    new BlockPos(0, -1, -1)
            };


            for (BlockPos offset : offsets) {
                BlockPos pos = playerPos.add(offset);
                if (canPlaceAnchor(BlockPos.ofFloored(pos.toCenterPos()))) {
                    anchorPos.add(BlockPos.ofFloored(pos.toCenterPos()));
                }
            }

        //return anchorPos.get(0);
        return null;
    }

    public boolean canPlaceAnchor(BlockPos p) {
        BlockState state = mc.world.getBlockState(p);
        if (!state.isOf(Blocks.OBSIDIAN) && !state.isOf(Blocks.BEDROCK)) {
            return false;
        }
        BlockPos p2 = p.up();
        BlockState state2 = mc.world.getBlockState(p2);
        if (!mc.world.isAir(p2) && !state2.isOf(Blocks.FIRE)) {
            return false;
        }
        return true;
    }

    public static float getTotalHealth() {
        return mc.player.getHealth() + mc.player.getAbsorptionAmount();
    }
    public static Vec3d vec3d(BlockPos pos) {
        return new Vec3d(pos.getX(), pos.getY(), pos.getZ());
    }

    private boolean checkMultitask() {
        return !multitaskConfig.getValue() && mc.player.isUsingItem()
                || !whileMiningConfig.getValue() && mc.interactionManager.isBreakingBlock();
    }


    @EventListener
    public void onRenderWorld(RenderWorldEvent event){

        if (!(renderpos == null)) {
            RenderManager.renderBox(event.getMatrices(), renderpos,
                    Modules.COLORS.getRGB(70));
            RenderManager.renderBoundingBox(event.getMatrices(), renderpos,
                    2.5f, Modules.COLORS.getRGB(70));
        }

    }
}


