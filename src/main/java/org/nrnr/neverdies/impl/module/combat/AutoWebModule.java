package org.nrnr.neverdies.impl.module.combat;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.config.setting.NumberConfig;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.BlockPlacerModule;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.impl.event.network.DisconnectEvent;
import org.nrnr.neverdies.impl.event.network.PlayerTickEvent;
import org.nrnr.neverdies.init.Managers;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chronos
 * @since 1.0
 */
public class AutoWebModule extends BlockPlacerModule {

    Config<Float> rangeConfig = new NumberConfig<>("PlaceRange", "The range to fill nearby holes", 0.1f, 4.0f, 6.0f);
    Config<Float> enemyRangeConfig = new NumberConfig<>("EnemyRange", "The maximum range of targets", 0.1f, 10.0f, 15.0f);
    Config<Boolean> rotateConfig = new BooleanConfig("Rotate", "Rotates to block before placing", false);
    Config<Boolean> coverHeadConfig = new BooleanConfig("CoverHead", "Places webs on the targets head", false);
    Config<Boolean> selfConfig = new BooleanConfig("Self", "Places webs yourself", false);
    Config<Integer> shiftTicksConfig = new NumberConfig<>("ShiftTicks", "The number of blocks to place per tick", 1, 2, 5);
    Config<Integer> shiftDelayConfig = new NumberConfig<>("ShiftDelay", "The delay between each block placement interval", 0, 1, 5);
    private int shiftDelay;

    public AutoWebModule() {
        super("AutoWeb", "Automatically traps nearby entities in webs", ModuleCategory.COMBAT);
    }

    @EventListener
    public void onDisconnect(DisconnectEvent event) {
        disable();
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event) {
        int blocksPlaced = 0;
        if (shiftDelay < shiftDelayConfig.getValue()) {
            shiftDelay++;
            return;
        }
        List<BlockPos> webs = new ArrayList<>();
        for (PlayerEntity entity : mc.world.getPlayers()) {
            if (entity == mc.player || Managers.SOCIAL.isFriend(entity.getName())) {
                continue;
            }
            double d = mc.player.distanceTo(entity);
            if (d > enemyRangeConfig.getValue()) {
                continue;
            }
            BlockPos feetPos = entity.getBlockPos();
            if (selfConfig.getValue()){
                BlockPos selfPos = mc.player.getBlockPos();
                double dist2 = mc.player.getEyePos().squaredDistanceTo(selfPos.toCenterPos());
                if (mc.world.getBlockState(selfPos).isAir() && dist2 <= ((NumberConfig) rangeConfig).getValueSq()) {
                    webs.add(selfPos);
                }
            }
            double dist = mc.player.getEyePos().squaredDistanceTo(feetPos.toCenterPos());
            if (mc.world.getBlockState(feetPos).isAir() && dist <= ((NumberConfig) rangeConfig).getValueSq()) {
                webs.add(feetPos);
            }
            if (coverHeadConfig.getValue()) {
                BlockPos headPos = feetPos.up();
                double dist2 = mc.player.getEyePos().squaredDistanceTo(headPos.toCenterPos());
                if (mc.world.getBlockState(headPos).isAir() && dist2 <= ((NumberConfig) rangeConfig).getValueSq()) {
                    webs.add(headPos);
                }
            }
        }
        while (blocksPlaced < shiftTicksConfig.getValue()) {
            if (blocksPlaced >= webs.size()) {
                break;
            }
            BlockPos targetPos = webs.get(blocksPlaced);
            blocksPlaced++;
            shiftDelay = 0;
            // All rotations for shift ticks must send extra packet
            // This may not work on all servers
            placeWeb(targetPos);
        }
    }

    private void placeWeb(BlockPos pos) {
        int slot = getBlockItemSlot(Blocks.COBWEB);
        if (slot == -1) {
            return;
        }
        Managers.INTERACT.placeBlock(pos, slot, grimConfig.getValue(), strictDirectionConfig.getValue(), false, (state, angles) ->
        {
            if (rotateConfig.getValue()) {
                if (state) {
                    Managers.ROTATION.setRotationSilent(angles[0], angles[1], grimConfig.getValue());
                } else {
                    Managers.ROTATION.setRotationSilentSync(grimConfig.getValue());
                }
            }
        });
    }
}
