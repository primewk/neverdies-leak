package org.nrnr.neverdies.impl.module.combat;

import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.config.setting.ColorConfig;
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
import org.nrnr.neverdies.impl.module.world.AutoMineModule;
import org.nrnr.neverdies.impl.module.world.PacketMineModule;
import org.nrnr.neverdies.init.Managers;
import org.nrnr.neverdies.init.Modules;
import org.nrnr.neverdies.util.EvictingQueue;
import org.nrnr.neverdies.util.chat.ChatUtil;
import org.nrnr.neverdies.util.player.RotationUtil;
import org.nrnr.neverdies.util.world.BlockUtil;
import org.nrnr.neverdies.util.world.ExplosionUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Deque;
import java.util.List;
import java.util.PriorityQueue;


public class AutoSelectMinerModule extends RotationModule {


    Config<Boolean> autoConfig = new BooleanConfig("Auto", "Automatically mines nearby players feet", false);
    Config<Boolean> autoRemineConfig = new BooleanConfig("AutoRemine", "Automatically remines mined blocks", true, () -> autoConfig.getValue());
    Config<Float> enemyRangeConfig = new NumberConfig<>("EnemyRange", "Range to search for targets", 1.0f, 5.0f, 10.0f, () -> autoConfig.getValue());
    Config<Float> rangeConfig = new NumberConfig<>("Range", "The range to mine blocks", 0.1f, 4.0f, 5.0f);
    Config<Boolean> rotateConfig = new BooleanConfig("Rotate", "Rotates when mining the block", true);

    private Deque<MiningData> miningQueue = new EvictingQueue<>(2);
    private long lastBreak;
    private boolean manualOverride;

    public AutoSelectMinerModule() {
        super("AutoSelectMiner", "Automatically mines blocks using any speedmine", ModuleCategory.COMBAT, 900);
    }

    @Override
    public String getModuleData() {
        if (!miningQueue.isEmpty()) {
            MiningData data = miningQueue.peek();
            return String.format("%.1f", Math.min(data.getBlockDamage(), 1.0f));
        }
        return super.getModuleData();
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
                if (dist <= enemyRangeConfig.getValue() && dist < minDistance) {
                    minDistance = dist;
                    playerTarget = entity;
                }
            }

            if (playerTarget != null) {
                PriorityQueue<AutoMineCalc> miningPositions = getMiningPosition(playerTarget);
                PriorityQueue<AutoMineCalc> miningPositionsNoAir = getNoAir(miningPositions, playerTarget);
                PriorityQueue<AutoMineCalc> cityPositions = autoRemineConfig.getValue() ? miningPositions : miningPositionsNoAir;

                if (!BlockUtil.canPlace(playerTarget.getBlockPos(), true)){
                    cityPositions.add(new AutoMineCalc(playerTarget.getBlockPos(), Double.MAX_VALUE));
                }

                while (!cityPositions.isEmpty()) {
                    AutoMineCalc cityBlockPos = cityPositions.poll();
                    if (cityBlockPos != null && !mc.world.isAir(cityBlockPos.pos()) && (Modules.PACKETMINE.damage == 0.0)) {
                        clickBlock(cityBlockPos.pos(), Direction.UP, true, rotateConfig.getValue());
                    }
                }
            }
        }
    }


    public void clickBlock(BlockPos clickpos, Direction direction, boolean swing, boolean rotate){
        mc.interactionManager.attackBlock(clickpos, direction);
        if (swing){
            mc.player.swingHand(Hand.MAIN_HAND);
        }

        if (rotate) {
            float rotations[] = RotationUtil.getRotationsTo(mc.player.getEyePos(), clickpos.toCenterPos());
            setRotation(rotations[0], rotations[1]);
            Managers.ROTATION.setRotationSilentSync(true);
        }

    }


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

    public record AutoMineCalc(BlockPos pos, double entityDamage) implements Comparable<AutoMineCalc> {

        @Override
        public int compareTo(@NotNull AutoMineCalc o) {
            return Double.compare(-entityDamage(), -o.entityDamage());
        }
    }



    private boolean isDataPacketMine(MiningData data) {
        return miningQueue.size() == 2 && data == miningQueue.getLast();
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
}