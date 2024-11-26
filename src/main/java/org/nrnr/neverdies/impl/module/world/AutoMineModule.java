package org.nrnr.neverdies.impl.module.world;

import com.google.common.collect.Lists;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EndCrystalItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.config.setting.ColorConfig;
import org.nrnr.neverdies.api.config.setting.EnumConfig;
import org.nrnr.neverdies.api.config.setting.NumberConfig;
import org.nrnr.neverdies.api.event.EventStage;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.RotationModule;
import org.nrnr.neverdies.api.render.RenderManager;
import org.nrnr.neverdies.impl.event.config.ConfigUpdateEvent;
import org.nrnr.neverdies.impl.event.network.AttackBlockEvent;
import org.nrnr.neverdies.impl.event.network.PacketEvent;
import org.nrnr.neverdies.impl.event.network.PlayerTickEvent;
import org.nrnr.neverdies.impl.event.render.RenderWorldEvent;
import org.nrnr.neverdies.impl.manager.player.interaction.RotationCallback;
import org.nrnr.neverdies.impl.manager.player.rotation.Rotation;
import org.nrnr.neverdies.impl.module.combat.AutoCrystalModule;
import org.nrnr.neverdies.init.Managers;
import org.nrnr.neverdies.init.Modules;
import org.nrnr.neverdies.util.EvictingQueue;
import org.nrnr.neverdies.util.chat.ChatUtil;
import org.nrnr.neverdies.util.player.FindItemResult;
import org.nrnr.neverdies.util.player.InventoryUtil;
import org.nrnr.neverdies.util.player.RotationUtil;
import org.nrnr.neverdies.util.world.BlockUtil;
import org.nrnr.neverdies.util.world.ExplosionUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.nrnr.neverdies.util.player.InventoryUtil.findInHotbar;


/**
 * @author LachCrafter
 * @since 1.0
 */
public class AutoMineModule extends RotationModule {

    Config<Boolean> multitaskConfig = new BooleanConfig("Multitask", "Allows mining while using items", false);
    Config<Boolean> autoConfig = new BooleanConfig("Auto", "Automatically mines nearby players feet", false);
    Config<Boolean> autoRemineConfig = new BooleanConfig("AutoRemine", "Automatically remines mined blocks", true, () -> autoConfig.getValue());
    Config<Boolean> strictDirectionConfig = new BooleanConfig("StrictDirection", "Only mines on visible faces", false, () -> autoConfig.getValue());
    Config<Float> enemyRangeConfig = new NumberConfig<>("EnemyRange", "Range to search for targets", 1.0f, 5.0f, 10.0f, () -> autoConfig.getValue());
    Config<Boolean> doubleBreakConfig = new BooleanConfig("DoubleBreak", "Allows you to mine two blocks at once", false);
    Config<Boolean> tripleBreakConfig = new BooleanConfig("TripleBreak", "Allows you to mine three blocks at once", false);
    Config<Boolean> autoFishConfig = new BooleanConfig("AutoFish", "Mines block below players feet if in phase", false);
    Config<Boolean> antiFishConfig = new BooleanConfig("AntiFish", "Mines block above head", false);
    Config<Boolean> bomber = new BooleanConfig("Bomber", "Mines block above head", false);
    Config<BomberSwap> autoSwapConfig = new EnumConfig<>("Bomber-Swap", "Swaps to an end crystal before placing if the player is not holding one", BomberSwap.NORMAL    , AutoMineModule.BomberSwap.values(), () -> bomber.getValue());
    Config<Float> rangeConfig = new NumberConfig<>("Range", "The range to mine blocks", 0.1f, 4.0f, 5.0f);
    Config<Float> speedConfig = new NumberConfig<>("Speed", "The speed to mine blocks", 0.1f, 1.0f, 1.0f);
    Config<AutoMineModule.Rotation> rotateConfig = new EnumConfig<>("Rotate", "Packet Rotate and Normal Rotate", Rotation.NORMAL, AutoMineModule.Rotation.values());
    Config<Boolean> switchResetConfig = new BooleanConfig("SwitchReset", "Resets mining after switching items", false);
    Config<Boolean> grimConfig = new BooleanConfig("Grim", "Uses grim block breaking speeds", false);
    Config<AutoMineModule.Swap> swapConfig = new EnumConfig<>("AutoSwap", "Swaps to the best tool once the mining is complete", AutoMineModule.Swap.SILENT, AutoMineModule.Swap.values());
    Config<Boolean> instantConfig = new BooleanConfig("Instant", "Instant remines mined blocks", true);
    Config<Boolean> growRender = new BooleanConfig("GrowRenders", "Grow Renders mined blocks", true);
    Config<Color> colorConfig = new ColorConfig("Color", "Only white/purplish works for now", new Color(0, 0, 0, 50), true, false);
    Config<Boolean> lineConfig = new BooleanConfig("Line", "Line or Box?", true);
    Config<Boolean> damageConfig = new BooleanConfig("Damage", "Render Damage", true);


    private Deque<MiningData> miningQueue = new EvictingQueue<>(2);
    private long lastBreak;
    private boolean manualOverride;

    public AutoMineModule() {
        super("AutoMine", "Automatically mines blocks", ModuleCategory.WORLD, 900);
    }

    @Override
    public String getModuleData() {
        if (!miningQueue.isEmpty()) {
            MiningData data = miningQueue.peek();
            return String.format("%.1f", Math.min(data.getBlockDamage(), 1.0f));
        }
        return super.getModuleData();
    }

    @Override
    public void onEnable() {
        if (doubleBreakConfig.getValue()) {
            miningQueue = new EvictingQueue<>(2);
        }
        else if (tripleBreakConfig.getValue()) {
            miningQueue = new EvictingQueue<>(3);
        } else {
            miningQueue = new EvictingQueue<>(1);
        }
    }

    @Override
    protected void onDisable() {
        miningQueue.clear();
        manualOverride = false;
        Managers.INVENTORY.syncToClient();
    }

    @EventListener
    public void onPlayerTick(final PlayerTickEvent event) {
        MiningData miningData = null;
        if (!miningQueue.isEmpty()) {
            miningData = miningQueue.getFirst();
        }
        if (autoConfig.getValue() && !manualOverride && (miningData == null || mc.world.isAir(miningData.getPos()))) {
            PlayerEntity playerTarget = null;
            double minDistance = Float.MAX_VALUE;
            for (PlayerEntity entity : mc.world.getPlayers()) {
                if (entity == mc.player || Managers.SOCIAL.isFriend(entity.getName())) {
                    continue;
                }
                double dist = mc.player.distanceTo(entity);
                if (dist > enemyRangeConfig.getValue()) {
                    continue;
                }
                if (dist < minDistance) {
                    minDistance = dist;
                    playerTarget = entity;
                }
            }
            if (playerTarget != null) {
                PriorityQueue<AutoMineCalc> miningPositions = getMiningPosition(playerTarget);
                PriorityQueue<AutoMineCalc> miningPositionsNoAir = getNoAir(miningPositions, playerTarget);
                PriorityQueue<AutoMineCalc> cityPositions = autoRemineConfig.getValue() ? miningPositions : miningPositionsNoAir;
                if (cityPositions.isEmpty()) {
                    return;
                }
                if (antiFishConfig.getValue() & mc.player.isCrawling()){
                    MiningData antiFish = new AutoMiningData(mc.player.getBlockPos().up(1),
                            strictDirectionConfig.getValue() ? Managers.INTERACT.getPlaceDirectionGrim(mc.player.getBlockPos().up(1)) : Direction.UP);
                    startMining(antiFish);
                    miningQueue.addFirst(antiFish);
                }
                BlockPos pos = playerTarget.getBlockPos();
                if (autoFishConfig.getValue() & !BlockUtil.canPlace(pos, false) & !BlockUtil.canPlace(pos.up(1), false)){
                    MiningData autoFish = new AutoMiningData(pos,
                            strictDirectionConfig.getValue() ? Managers.INTERACT.getPlaceDirectionGrim(pos) : Direction.UP);
                    startMining(autoFish);
                    miningQueue.addFirst(autoFish);
                }

                if (doubleBreakConfig.getValue()) {
                    final AutoMineCalc cityPos = cityPositions.poll();
                    if (cityPos != null) {
                        AutoMineCalc cityPos2 = null;
                        miningPositionsNoAir.removeIf(c -> c.pos().equals(cityPos.pos()));
                        if (!miningPositionsNoAir.isEmpty()) {
                            cityPos2 = miningPositionsNoAir.poll();
                        }
                        if (cityPos2 != null) {

                            if (!mc.world.isAir(cityPos.pos()) && !mc.world.isAir(cityPos2.pos()) && !isBlockDelayGrim()) {
                                MiningData data1 = new AutoMiningData(cityPos2.pos(),
                                                strictDirectionConfig.getValue() ? Managers.INTERACT.getPlaceDirectionGrim(cityPos2.pos()) : Direction.UP);
                                MiningData data2 = new AutoMiningData(cityPos.pos(),
                                        strictDirectionConfig.getValue() ? Managers.INTERACT.getPlaceDirectionGrim(cityPos.pos()) : Direction.UP);
                                MiningData fish = new AutoMiningData(mc.player.getBlockPos().up(1),
                                        strictDirectionConfig.getValue() ? Managers.INTERACT.getPlaceDirectionGrim(mc.player.getBlockPos().up(1)) : Direction.UP);

                                /*if (autoFishConfig.getValue()){
                                    if (mc.player.isCrawling()) {
                                        startMining(fish);
                                        startMining(data1);
                                    }
                                }
                               */ //else{
                                    startMining(data1);
                                    startMining(data2);
                                //}
                                /*if (autoFishConfig.getValue()){
                                    if (mc.player.isCrawling()){
                                        miningQueue.addFirst(fish);
                                        miningQueue.addFirst(data1);
                                    }
                                }
                               */// else {
                                    miningQueue.addFirst(data1);
                                    miningQueue.addFirst(data2);
                               // }

                            }
                        } else {
                            // If we are re-mining, bypass throttle check below
                            if (!mc.world.isAir(cityPos.pos()) && !isBlockDelayGrim()) {
                                MiningData data = new AutoMiningData(cityPos.pos(),
                                        strictDirectionConfig.getValue() ? Managers.INTERACT.getPlaceDirectionGrim(cityPos.pos()) : Direction.UP);
                                startMining(data);
                                miningQueue.addFirst(data);
                            }
                        }
                    }

                } else {
                    final AutoMineCalc cityBlockPos = cityPositions.poll();
                    if (cityBlockPos != null && !isBlockDelayGrim()) {
                        // If we are re-mining, bypass throttle check below
                        if (miningData instanceof AutoMiningData && miningData.isInstantRemine() && !mc.world.isAir(miningData.getPos()) && autoRemineConfig.getValue()) {
                            stopMining(miningData);
                        } else if (!mc.world.isAir(cityBlockPos.pos()) && !isBlockDelayGrim()) {
                            MiningData data = new AutoMiningData(cityBlockPos.pos(),
                                    strictDirectionConfig.getValue() ? Managers.INTERACT.getPlaceDirectionGrim(cityBlockPos.pos()) : Direction.UP);
                            startMining(data);
                            miningQueue.addFirst(data);
                        }
                    }
                }
            }
        }
        if (miningQueue.isEmpty()) {
            return;
        }
        for (MiningData data : miningQueue) {
            if (isDataPacketMine(data) && (data.getState().isAir() || data.getBlockDamage() >= 1.5f)) {
                Managers.INVENTORY.syncToClient();
                miningQueue.remove(data);
                return;
            }
            final float damageDelta = Modules.PACKETMINE.calcBlockBreakingDelta(
                    data.getState(), mc.world, data.getPos());
            data.damage(damageDelta);
            if (data.getBlockDamage() >= 1.0f && isDataPacketMine(data)) {
                if (mc.player.isUsingItem() && !multitaskConfig.getValue()) {
                    return;
                }
                if (data.getSlot() != -1) {
                    Managers.INVENTORY.setSlot(data.getSlot());
                }
            }
        }
        MiningData miningData2 = miningQueue.getFirst();
        if (miningData2 != null) {
            final double distance = mc.player.getEyePos().squaredDistanceTo(miningData2.getPos().toCenterPos());
            if (distance > ((NumberConfig<Float>) rangeConfig).getValueSq()) {
                //            abortMining(miningData);
                miningQueue.remove(miningData2);
                return;
            }
            if (miningData2.getState().isAir()) {
                // Once we broke the block that overrode that the auto city, we can allow the module
                // to auto mine "city" blocks
                if (manualOverride) {
                    manualOverride = false;
                    miningQueue.remove(miningData2);
                    return;
                }
                if (instantConfig.getValue()) {
                    if (miningData2 instanceof AutoMiningData && !autoRemineConfig.getValue()) {
                        miningQueue.remove(miningData2);
                        return;
                    }
                    miningData2.setInstantRemine();
                    miningData2.setDamage(1.0f);
                } else {
                    miningData2.resetDamage();
                }
                return;
            }
            if (miningData2.getBlockDamage() >= speedConfig.getValue() || miningData2.isInstantRemine()) {
                if (mc.player.isUsingItem() && !multitaskConfig.getValue()) {
                    return;
                }
                stopMining(miningData2);
            }
        }
    }

    @EventListener
    public void onAttackBlock(final AttackBlockEvent event) {
        // Do not try to break unbreakable blocks
        if (event.getState().getBlock().getHardness() == -1.0f || event.getState().isAir() || mc.player.isCreative()) {
            return;
        }
        event.cancel();
        int queueSize = miningQueue.size();
        if (queueSize == 0) {
            attemptMine(event.getPos(), event.getDirection());
        } else if (queueSize == 1) {
            MiningData data = miningQueue.getFirst();
            if (data.getPos().equals(event.getPos())) {
//              abortMining(miningData);
                return;
            }
            // Only count as an override if AutoCity is doing something
            if (data instanceof AutoMiningData) {
                manualOverride = true;
            }
            attemptMine(event.getPos(), event.getDirection());
        } else if (queueSize == 2) {
            MiningData data1 = miningQueue.getFirst();
            MiningData data2 = miningQueue.getLast();
            if (data1.getPos().equals(event.getPos()) || data2.getPos().equals(event.getPos())) {
//              abortMining(miningData);
                return;
            }
            if (data1 instanceof AutoMiningData || data2 instanceof AutoMiningData) {
                manualOverride = true;
            }
            attemptMine(event.getPos(), event.getDirection());
        }
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event) {
        if (event.getPacket() instanceof UpdateSelectedSlotC2SPacket && switchResetConfig.getValue()) {
            for (MiningData data : miningQueue) {
                data.resetDamage();
            }
        }
    }

    @EventListener
    public void onRenderWorld(final RenderWorldEvent event) {
        for (MiningData data : miningQueue) {
            renderMiningData(event.getMatrices(), data);
        }
    }

    private void renderMiningData(MatrixStack matrixStack, MiningData data) {
        if (data != null && !mc.player.isCreative() && data.getBlockDamage() > 0.01f) {
            float miningSpeed = isDataPacketMine(data) ? 1.0f : speedConfig.getValue();
            BlockPos mining = data.getPos();
            VoxelShape outlineShape = VoxelShapes.fullCube();
            if (!data.isInstantRemine()) {
                outlineShape = data.getState().getOutlineShape(mc.world, mining);
                outlineShape = outlineShape.isEmpty() ? VoxelShapes.fullCube() : outlineShape;
            }
            Box render1 = outlineShape.getBoundingBox();
            Box render = new Box(mining.getX() + render1.minX, mining.getY() + render1.minY,
                    mining.getZ() + render1.minZ, mining.getX() + render1.maxX,
                    mining.getY() + render1.maxY, mining.getZ() + render1.maxZ);
            Vec3d center = render.getCenter();
            float scale = MathHelper.clamp(data.getBlockDamage() / miningSpeed, 0, 1.0f);
            double dx = (render1.maxX - render1.minX) / 2.0;
            double dy = (render1.maxY - render1.minY) / 2.0;
            double dz = (render1.maxZ - render1.minZ) / 2.0;
            final Box scaled = new Box(center, center).expand(dx * scale, dy * scale, dz * scale);
            if (!growRender.getValue())
            {
                if (lineConfig.getValue()){
                    RenderManager.renderBoundingBox(matrixStack, scaled,
                            5f, data.getBlockDamage() > (0.95f * miningSpeed) ? colorConfig.getValue().getRGB() : colorConfig.getValue().getRGB()   );
                }
                else{
                    RenderManager.renderBox(matrixStack, scaled,
                            data.getBlockDamage() > (0.95f * miningSpeed) ? colorConfig.getValue().getRGB() : colorConfig.getValue().getRGB());

                    RenderManager.renderBoundingBox(matrixStack, scaled,
                            2.5F, data.getBlockDamage() > (0.95f * miningSpeed) ? colorConfig.getValue().getRGB() : colorConfig.getValue().getRGB()   );
                }

               // ChatUtil.clientSendMessage(String.valueOf(data.getBlockDamage()));
            }
            else{
                if (lineConfig.getValue()){

                    RenderManager.renderBoundingBox(matrixStack, scaled,
                            5f, data.getBlockDamage() > (0.95f * miningSpeed) ? 0x6000ff00 : 0x60ff0000);
                }
                else{
                    RenderManager.renderBox(matrixStack, scaled,
                            data.getBlockDamage() > (0.95f * miningSpeed) ? 0x6000ff00 : 0x60ff0000);
                    RenderManager.renderBoundingBox(matrixStack, scaled,
                            2.5f, data.getBlockDamage() > (0.95f * miningSpeed) ? 0x6000ff00 : 0x60ff0000);
                }

            }

            if (bomber.getValue()){
                if (!(renderBomberPos == null)){

                }

            }

            if (damageConfig.getValue()) {
                DecimalFormat format = new DecimalFormat("0");
                Vec3d boxCenter = scaled.getCenter();
                RenderManager.post(() -> {
                    float damagePercent = data.getBlockDamage() / this.speedConfig.getValue().floatValue() * 100.0f;
                    String damagePercentage = format.format(damagePercent > 100.0f ? 100.0 : (double)damagePercent) + "%";
                    RenderManager.renderSign(matrixStack, damagePercentage, boxCenter);
                });
            }

        }
    }

    @EventListener
    public void onConfigUpdate(ConfigUpdateEvent event) {
        if (event.getStage() == EventStage.POST && event.getConfig() == doubleBreakConfig) {
            if (doubleBreakConfig.getValue()) {
                miningQueue = new EvictingQueue<>(2);
            } else {
                miningQueue = new EvictingQueue<>(1);
            }
        }
    }

    // LOL
    private PriorityQueue<AutoMineCalc> getNoAir(PriorityQueue<AutoMineCalc> calcs, PlayerEntity player) {
        PriorityQueue<AutoMineCalc> noAir = new PriorityQueue<>();
        for (AutoMineCalc calc : calcs) {
            if (mc.world.isAir(calc.pos())) {
                continue;
            }
            noAir.add(calc);
        }
        noAir.removeIf(c -> c.pos().equals(player.getBlockPos()));
        return noAir;
    }

    private PriorityQueue<AutoMineCalc> getMiningPosition(PlayerEntity entity) {
        List<BlockPos> entityIntersections = Modules.SURROUND.getSurroundEntities(entity);
        PriorityQueue<AutoMineCalc> miningPositions = new PriorityQueue<>();
        for (BlockPos blockPos : entityIntersections) {
            double dist = mc.player.getEyePos().squaredDistanceTo(blockPos.toCenterPos());
            if (dist > ((NumberConfig<Float>) rangeConfig).getValueSq()) {
                continue;
            }
            if (!mc.world.getBlockState(blockPos).isReplaceable()) {
                miningPositions.add(new AutoMineCalc(blockPos, Double.MAX_VALUE));
            }
        }
        List<BlockPos> surroundBlocks = Modules.SURROUND.getEntitySurroundNoSupport(entity);
        for (BlockPos blockPos : surroundBlocks) {
            double dist = mc.player.getEyePos().squaredDistanceTo(blockPos.toCenterPos());
            if (dist > ((NumberConfig<Float>) rangeConfig).getValueSq()) {
                continue;
            }
            double damage = ExplosionUtil.getDamageTo(entity, blockPos.toCenterPos().subtract(0.0, -0.5, 0.0), true);
            miningPositions.add(new AutoMineCalc(blockPos, damage));
        }
        return miningPositions;
    }

    private record AutoMineCalc(BlockPos pos, double entityDamage) implements Comparable<AutoMineCalc> {

        @Override
        public int compareTo(@NotNull AutoMineCalc o) {
            return Double.compare(-entityDamage(), -o.entityDamage());
        }
    }

    private void attemptMine(BlockPos pos, Direction direction) {
        if (isBlockDelayGrim()) {
            return;
        }
        MiningData miningData = new MiningData(pos, direction);
        startMining(miningData);
        miningQueue.addFirst(miningData);
    }


    private int getCrystalSlot() {
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof EndCrystalItem) {
                slot = i;
                break;
            }
        }
        return slot;
    }


    public int prevslot = 0;

    private void startMining(MiningData data) {
        assert mc.player != null;
        prevslot = mc.player.getInventory().selectedSlot;
        if (data.getState().isAir() || data.isStarted()) {
            return;
        }

        Managers.NETWORK.sendSequencedPacket(id -> new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection(), id));
        Managers.NETWORK.sendSequencedPacket(id -> new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, data.getPos(), data.getDirection(), id));
        // packet mine
        if (doubleBreakConfig.getValue()) {
            Managers.NETWORK.sendSequencedPacket(id -> new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection(), id));
        }

        data.setStarted();
    }


    public boolean canUseCrystalOnBlock(BlockPos p) {
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

    public BlockPos renderBomberPos;

    //for bomber
    private void placeCrystal(BlockPos pos) {
        FindItemResult endResult = findInHotbar(Items.END_CRYSTAL);
        renderBomberPos = pos;

        if (autoSwapConfig.getValue() == BomberSwap.NORMAL){
                Managers.INVENTORY.setClientSlot(endResult.slot());
        }
        else if (autoSwapConfig.getValue() == BomberSwap.SILENT) {
            Managers.INVENTORY.setSlot(endResult.slot());
        }
        else if (autoSwapConfig.getValue() == BomberSwap.SILENT_ALT){
            mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                    endResult.slot() + 36, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
        }
        

        Modules.AUTO_CRYSTAL.placeCrystal(pos, Hand.MAIN_HAND);
        if (rotateConfig.getValue() == Rotation.NORMAL) {
            float rotations[] = RotationUtil.getRotationsTo(mc.player.getEyePos(), pos.toCenterPos());
            setRotation(rotations[0], rotations[1]);
        }

        if (autoSwapConfig.getValue() == BomberSwap.SILENT_ALT) {
            mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                    endResult.slot() + 36, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);

        } else if (autoSwapConfig.getValue() == BomberSwap.SILENT) {
            Managers.INVENTORY.syncToClient();
        }
    }




    private void abortMining(MiningData data) {
        if (!data.isStarted() || data.getState().isAir() || data.isInstantRemine() || data.getBlockDamage() >= 1.0f) {
            return;
        }
        Managers.NETWORK.sendSequencedPacket(id -> new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, data.getPos(), data.getDirection(), id));
        Managers.INVENTORY.syncToClient();
    }

    private void stopMining(MiningData data) {
        if (!data.isStarted() || data.getState().isAir()) {
            return;
        }


        if (bomber.getValue()){
            ArrayList<BlockPos> crystalPositions = new ArrayList<>();
            BlockPos miningPos = data.getPos();
            PlayerEntity playerTarget = null;
            double minDistance = Float.MAX_VALUE;
            for (PlayerEntity entity : mc.world.getPlayers()) {
                if (entity == mc.player || Managers.SOCIAL.isFriend(entity.getName())) {
                    continue;
                }
                double dist = mc.player.distanceTo(entity);
                if (dist > enemyRangeConfig.getValue()) {
                    continue;
                }
                if (dist < minDistance) {
                    minDistance = dist;
                    playerTarget = entity;
                }
            }

            //cev

            if (!(playerTarget == null)){
                if (miningPos.toCenterPos() == playerTarget.getBlockPos().up(2).toCenterPos()){
                    if (canUseCrystalOnBlock(BlockPos.ofFloored(miningPos.toCenterPos()))){
                        crystalPositions.add(BlockPos.ofFloored(miningPos.toCenterPos()));
                    }
                }
            }

            if (canUseCrystalOnBlock(BlockPos.ofFloored(miningPos.north(1).down(1).toCenterPos()))) {
                crystalPositions.add(BlockPos.ofFloored(miningPos.north(1).down(1).toCenterPos()));
            }
            if (canUseCrystalOnBlock(BlockPos.ofFloored(miningPos.south(1).down(1).toCenterPos()))) {
                crystalPositions.add(BlockPos.ofFloored(miningPos.south(1).down(1).toCenterPos()));
            }
            if (canUseCrystalOnBlock(BlockPos.ofFloored(miningPos.east(1).down(1).toCenterPos()))) {
                crystalPositions.add(BlockPos.ofFloored(miningPos.east(1).down(1).toCenterPos()));
            }
            if (canUseCrystalOnBlock(BlockPos.ofFloored(miningPos.west(1).down(1).toCenterPos()))) {
                crystalPositions.add(BlockPos.ofFloored(miningPos.west(1).down(1).toCenterPos()));
            }

            for (BlockPos pos : crystalPositions) {
                if (!crystalPositions.isEmpty()) {
                    BlockPos firstPos = crystalPositions.get(0);
                    placeCrystal(firstPos);
                }
            }
        }

        int slot = data.getSlot();

        boolean canSwap = data.getSlot() != -1;

        if (canSwap & swapConfig.getValue() == Swap.SILENT) {
            Managers.INVENTORY.setSlot(data.getSlot());
        }

        if (swapConfig.getValue() == Swap.ALTERNATE || slot >= 9) {
            switchTo(slot, -1);
        }
        if (rotateConfig.getValue() == Rotation.NORMAL) {
            float rotations[] = RotationUtil.getRotationsTo(mc.player.getEyePos(), data.getPos().toCenterPos());
            setRotation(rotations[0], rotations[1]);
        }
        else if (rotateConfig.getValue() == Rotation.PACKET){
            float rotations[] = RotationUtil.getRotationsTo(mc.player.getEyePos(), data.getPos().toCenterPos());
            setRotationSilent(rotations[0], rotations[1]);
        }
        Managers.NETWORK.sendSequencedPacket(id -> new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection(), id));
        lastBreak = System.currentTimeMillis();
        if (canSwap & swapConfig.getValue() == Swap.SILENT) {
            Managers.INVENTORY.syncToClient();
        }
        if (swapConfig.getValue() == Swap.ALTERNATE || slot >= 9) {
            switchTo(prevslot, slot);
        }
        if (rotateConfig.getValue() == Rotation.NORMAL) {
            Managers.ROTATION.setRotationSilentSync(true);
        }
        else if (rotateConfig.getValue() == Rotation.PACKET) {
            Managers.ROTATION.setRotationSilentSync(true);
        }
    }

    private void switchTo(int slot, int from) {
        if (swapConfig.getValue() == Swap.ALTERNATE || slot >= 9) {
            if (from == -1)
                clickSlot(slot < 9 ? slot + 36 : slot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP);
            else
                clickSlot(from < 9 ? from + 36 : from, mc.player.getInventory().selectedSlot, SlotActionType.SWAP);
            closeScreen();
            ;
        }
    }

    protected void sendPacket(Packet<?> packet) {
        if (mc.getNetworkHandler() == null) return;

        mc.getNetworkHandler().sendPacket(packet);
    }
    public static void clickSlot(int id, int button, SlotActionType type) {
        if (id == -1 || mc.interactionManager == null || mc.player == null) return;
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, id, button, type, mc.player);
    }


    private void closeScreen() {
        if (mc.player == null) return;

        sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
    }

    private boolean isDataPacketMine(MiningData data) {
        return miningQueue.size() == 2 && data == miningQueue.getLast();
    }

    // https://github.com/GrimAnticheat/Grim/blob/2.0/src/main/java/ac/grim/grimac/checks/impl/misc/FastBreak.java#L80
    public boolean isBlockDelayGrim() {
        return System.currentTimeMillis() - lastBreak <= 280 && grimConfig.getValue();
    }

    public static class AutoMiningData extends MiningData {

        public AutoMiningData(BlockPos pos, Direction direction) {
            super(pos, direction);
        }
    }

    public static class MiningData {

        private final BlockPos pos;
        private final Direction direction;
        private float blockDamage;
        private boolean instantRemine;
        private boolean started;

        public MiningData(BlockPos pos, Direction direction) {
            this.pos = pos;
            this.direction = direction;
        }

        public boolean isInstantRemine() {
            return instantRemine;
        }

        public void setInstantRemine() {
            this.instantRemine = true;
        }

        public float damage(final float dmg) {
            blockDamage += dmg;
            return blockDamage;
        }

        public void setDamage(float blockDamage) {
            this.blockDamage = blockDamage;
        }

        public void resetDamage() {
            instantRemine = false;
            blockDamage = 0.0f;
        }

        public BlockPos getPos() {
            return pos;
        }

        public Direction getDirection() {
            return direction;
        }

        public int getSlot() {
            return Modules.AUTO_TOOL.getBestToolNoFallback(getState());
        }

        public BlockState getState() {
            return mc.world.getBlockState(pos);
        }

        public boolean isStarted() {
            return started;
        }

        public void setStarted() {
            this.started = true;
        }

        public float getBlockDamage() {
            return blockDamage;
        }


    }
    public enum Swap {
        SILENT,
        ALTERNATE,
        OFF
    }
    public enum Rotation {
        NORMAL,
        PACKET
    }
    public enum BomberSwap {
        NORMAL,
        SILENT,
        SILENT_ALT,
        OFF
    }


}