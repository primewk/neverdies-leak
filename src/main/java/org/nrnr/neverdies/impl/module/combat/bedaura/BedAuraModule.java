package org.nrnr.neverdies.impl.module.combat.bedaura;

import net.minecraft.block.BedBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BedItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
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
import org.nrnr.neverdies.impl.manager.player.rotation.Rotation;
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
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
  created by  chronos
 */

public class BedAuraModule extends RotationModule {

    Config<Boolean> multitaskConfig = new BooleanConfig("Multitask", "Allows attacking while using items", true);
    Config<Boolean> whileMiningConfig = new BooleanConfig("WhileMining", "Allows attacking while mining blocks", true);
    Config<Float> targetRangeConfig = new NumberConfig<>("EnemyRange", "Range to search for potential enemies", 1.0f, 10.0f, 13.0f);
    Config<Float> maxSelfDamage = new NumberConfig<>("Self Damage", "Amount of Self Damage", 1.0f, 10.0f, 30.0f);
    Config<Float> minDamage = new NumberConfig<>("Min Damage", "Amount of Min Damage", 1.0f, 5.0f, 30.0f);
    Config<Integer> delay = new NumberConfig<>("Delay", "Place Delay", 0, 1, 10);
    Config<Swap> autoSwapConfig = new EnumConfig<>("Swap", "Swaps to an bed before placing if the player is not holding one",Swap.NORMAL, Swap.values());
    Config<Boolean> placeConfig = new BooleanConfig("Place", "Place Bed", true);
    Config<Boolean> breakConfig = new BooleanConfig("Break", "Break/Attack Bed", false);
    Config<Boolean> rotateConfig = new BooleanConfig("Rotate", "Simple Rotate", true);
    Config<Boolean> playersConfig = new BooleanConfig("Players", "Attack Players", true);
    Config<Boolean> monstersConfig = new BooleanConfig("Monsters", "Attack Monsters", false);
    Config<Boolean> neutralsConfig = new BooleanConfig("Neutrals", "Attack Neutrals", false);
    Config<Boolean> animalsConfig = new BooleanConfig("Animals", "Attack Animals", false);
    Config<Boolean> autoMoveConfig = new BooleanConfig("AutoMove", "Move in Hotbar", false);
    Config<Integer> autoMoveSlot = new NumberConfig<>("AutoMove-Slot", "Bed Slot", 0, 4, 9);
    Config<Boolean> antiSuicideConfig = new BooleanConfig("Anti-Suicide", "Don't kill yourself ig.", false);
    Config<Boolean> blockDestructionConfig = new BooleanConfig("nothing", "Accounts for explosion block destruction when calculating damages", true);
    Config<Boolean> strictDirectionConfig = new BooleanConfig("StrictDirection", "Interacts with only visible directions when placing crystals", true);
    Config<Boolean> exposedDirectionConfig = new BooleanConfig("StrictDirection-Exposed", "Interacts with only exposed directions when placing crystals", true);
    private final Timer autoSwapTimer = new CacheTimer();
    private final Map<BlockPos, Long> placePackets =
            Collections.synchronizedMap(new ConcurrentHashMap<>());

    public BedAuraModule() {
        super("BedAura", "Automatically places and explodes beds",
                ModuleCategory.COMBAT);
    }

    private long lastPlaceTime = 0;
    private long lastPlaceTime2 = 0;
    private final Timer lastSwapTimer = new CacheTimer();
    private BlockPos renderpos;
    private int timer;
    private BlockPos placePos, breakPos;

    @EventListener
    public void onEnable(){
        timer = delay.getValue();
    }

    @EventListener
    public void onPlayerTick(final PlayerTickEvent event) {
        if (mc.world.getDimension().bedWorks()) {
            ChatUtil.clientSendMessage("You can't blow up beds in this dimension, disabling.");
            toggle();
            return;
        }
        lastSwapTimer.reset();
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

            if (autoMoveConfig.getValue()) {
                FindItemResult bed = InventoryUtil.find(itemStack -> itemStack.getItem() instanceof BedItem);

                if (bed.found() && bed.slot() != autoMoveSlot.getValue() - 1) {
                    InventoryUtil.move().from(bed.slot()).toHotbar(autoMoveSlot.getValue() - 1);
                }
            }

            if (breakPos == null) {
                placePos = findPlace(playerTarget);
            }

            if (timer <= 0 && placeBed(placePos)) {
                if (breakConfig.getValue()){
                    breakBed(placePos);
                }
                renderpos = placePos;
                timer = delay.getValue();
            }
            else {
                timer--;
            }


        }
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event){

        if (!(renderpos == null)) {
            //RenderManager.renderBoundingBox(event.getMatrices(), renderpos, 1.0f, Modules.COLORS.getRGB());
            RenderManager.renderBox(event.getMatrices(), renderpos,
                   Modules.COLORS.getRGB(70));
            RenderManager.renderBoundingBox(event.getMatrices(), renderpos,
                    2.5f, Modules.COLORS.getRGB(70));
        }

    }

    private void breakBed(BlockPos pos) {
        if (pos == null) return;
        breakPos = null;

        if (!(mc.world.getBlockState(pos).getBlock() instanceof BedBlock)) return;

        boolean wasSneaking = mc.player.isSneaking();
        if (wasSneaking) mc.player.setSneaking(false);

        mc.interactionManager.interactBlock(mc.player, Hand.OFF_HAND, new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false));

        mc.player.setSneaking(wasSneaking);
    }


    private BlockPos findBreak() {
        assert mc.player != null;
        BlockPos bedPos = findNearestBed(mc.player.getBlockPos(), 100);
        if (bedPos == null) {
            return null;
        }

        Vec3d bedVec = new Vec3d(bedPos.getX() + 0.5, bedPos.getY() + 0.5, bedPos.getZ() + 0.5);

        PlayerEntity playerTarget = null;
        double minDistance = Double.MAX_VALUE;

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
        }

        if (playerTarget != null) {
            double highest = -1;

            if (ExplosionUtil.bedDamage(playerTarget, bedVec) >= minDamage.getValue()
                    && ExplosionUtil.bedDamage(mc.player, bedVec) < maxSelfDamage.getValue()
                    && (!antiSuicideConfig.getValue() || getTotalHealth() - ExplosionUtil.bedDamage(mc.player, bedVec) > 0)) {
                return bedPos;
            }
        }

        return null;
    }

    private BlockPos findNearestBed(BlockPos start, int radius) {
        BlockPos nearestBed = null;
        double nearestDistance = Double.MAX_VALUE;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = start.add(x, y, z);
                    if (mc.world.getBlockState(pos).getBlock() == Blocks.BLUE_BED) {
                        double distance = pos.getSquaredDistance(start);
                        if (distance < nearestDistance) {
                            nearestDistance = distance;
                            nearestBed = pos;
                        }
                    }
                }
            }
        }
        return nearestBed;
    }
    



    private boolean placeBed(BlockPos blockPos) {

        if (blockPos == null) return false;

        FindItemResult bed = InventoryUtil.findInHotbar(itemStack -> itemStack.getItem() instanceof BedItem);
        if (bed.getHand() == null && !autoSwapConfig.getValue().equals(Swap.NORMAL)) return false;

        Managers.INTERACT.placeBlock(blockPos, bed.slot(), false, strictDirectionConfig.getValue(), false, (state, angles) ->
        {
            if (rotateConfig.getValue()) {
                if (state) {
                    Managers.ROTATION.setRotationSilent(angles[0], angles[1], false);
                } else {
                    Managers.ROTATION.setRotationSilentSync(true);
                }
            }
        });

        return true;



    }


    private CardinalDirection direction;

    private BlockPos findPlace(PlayerEntity target) {

        for (int index = 0; index < 3; index++) {
            int i = index == 0 ? 1 : index == 1 ? 0 : 2;

            for (CardinalDirection dir : CardinalDirection.values()) {
                if (strictDirectionConfig.getValue()
                        && dir.toDirection() != mc.player.getHorizontalFacing()
                        && dir.toDirection().getOpposite() != mc.player.getHorizontalFacing()) continue;

                BlockPos centerPos = target.getBlockPos().up(i);

                double headSelfDamage = ExplosionUtil.bedDamage(mc.player, vec3d(centerPos));
                double offsetSelfDamage = ExplosionUtil.bedDamage(mc.player, vec3d(centerPos.offset(dir.toDirection())));

                if (mc.world.getBlockState(centerPos).isReplaceable()
                        && BlockUtil.canPlace(centerPos.offset(dir.toDirection()), true)
                        && ExplosionUtil.bedDamage(target, vec3d(centerPos)) >= maxSelfDamage.getValue()
                        && offsetSelfDamage < maxSelfDamage.getValue()
                        && headSelfDamage < maxSelfDamage.getValue()
                        && (!antiSuicideConfig.getValue() || getTotalHealth() - headSelfDamage > 0)
                        && (!antiSuicideConfig.getValue() || getTotalHealth() - offsetSelfDamage > 0)) {
                    return centerPos.offset((direction = dir).toDirection());
                }
            }
        }

        return null;
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
    private boolean isHoldingBed() {
        if (!checkMultitask() && (autoSwapConfig.getValue() == Swap.SILENT)) {
            return true;
        }
        return getBedHand() != null;
    }

    private void placeInternal(BlockHitResult result, Hand hand) {
        if (hand == null) {
            return;
        }
        Managers.NETWORK.sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(hand, result, id));

            mc.player.swingHand(hand);

    }

    @Override
    public void onDisable() {

        placePackets.clear();

    }

    private boolean isSilentSwap(Swap swap) {
        return swap == Swap.SILENT;
    }

    private int getBedSlot() {
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof BedItem) {
                slot = i;
                break;
            }
        }
        return slot;
    }

    private Hand getBedHand() {
        final ItemStack offhand = mc.player.getOffHandStack();
        final ItemStack mainhand = mc.player.getMainHandStack();
        if (offhand.getItem() instanceof BedItem) {
            return Hand.OFF_HAND;
        } else if (mainhand.getItem() instanceof BedItem) {
            return Hand.MAIN_HAND;
        }
        return null;
    }

    private Direction getPlaceDirection(BlockPos blockPos) {
        if (!(blockPos == null)){
            int x = blockPos.getX();
            int y = blockPos.getY();
            int z = blockPos.getZ();
            if (strictDirectionConfig.getValue()) {
                if (mc.player.getY() >= blockPos.getY()) {
                    return Direction.UP;
                }
                BlockHitResult result = mc.world.raycast(new RaycastContext(
                        mc.player.getEyePos(), new Vec3d(x + 0.5, y + 0.5, z + 0.5),
                        RaycastContext.ShapeType.OUTLINE,
                        RaycastContext.FluidHandling.NONE, mc.player));
                if (result != null && result.getType() == HitResult.Type.BLOCK) {
                    Direction direction = result.getSide();
                    if (!exposedDirectionConfig.getValue() || mc.world.isAir(blockPos.offset(direction))) {
                        return direction;
                    }
                }
            } else {
                if (mc.world.isInBuildLimit(blockPos)) {
                    return Direction.DOWN;
                }
                BlockHitResult result = mc.world.raycast(new RaycastContext(
                        mc.player.getEyePos(), new Vec3d(x + 0.5, y + 0.5, z + 0.5),
                        RaycastContext.ShapeType.OUTLINE,
                        RaycastContext.FluidHandling.NONE, mc.player));
                if (result != null && result.getType() == HitResult.Type.BLOCK) {
                    return result.getSide();
                }
            }
        }
        return Direction.UP;
    }


    private boolean isValidTarget(Entity e) {
        return e instanceof PlayerEntity && playersConfig.getValue()
                || EntityUtil.isMonster(e) && monstersConfig.getValue()
                || EntityUtil.isNeutral(e) && neutralsConfig.getValue()
                || EntityUtil.isPassive(e) && animalsConfig.getValue();
    }
    public enum Swap {
        NORMAL,
        SILENT,
        OFF
    }
}


