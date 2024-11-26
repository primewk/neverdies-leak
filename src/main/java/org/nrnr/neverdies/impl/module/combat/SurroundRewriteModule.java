package org.nrnr.neverdies.impl.module.combat;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.config.setting.NumberConfig;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ObsidianPlacerModule;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.render.RenderManager;
import org.nrnr.neverdies.impl.event.network.DisconnectEvent;
import org.nrnr.neverdies.impl.event.network.PacketEvent;
import org.nrnr.neverdies.impl.event.network.PlayerTickEvent;
import org.nrnr.neverdies.impl.event.render.RenderWorldEvent;
import org.nrnr.neverdies.impl.event.world.AddEntityEvent;
import org.nrnr.neverdies.impl.event.world.RemoveEntityEvent;
import org.nrnr.neverdies.init.Managers;
import org.nrnr.neverdies.init.Modules;
import org.nrnr.neverdies.util.math.position.PositionUtil;
import org.nrnr.neverdies.util.player.PlayerUtil;
import org.nrnr.neverdies.util.render.animation.Animation;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author chronos
 * @since 1.0
 */
public class SurroundRewriteModule extends ObsidianPlacerModule {

    Config<Float> placeRangeConfig = new NumberConfig<>("PlaceRange", "The placement range for surround", 0.0f, 4.0f, 6.0f);
    Config<Boolean> rotateConfig = new BooleanConfig("Rotate", "Rotates to block before placing", true);
    Config<Boolean> attackConfig = new BooleanConfig("Attack", "Attacks crystals in the way of surround", true);
    Config<Boolean> centerConfig = new BooleanConfig("Center", "Centers the player before placing blocks", false);
    Config<Boolean> extendConfig = new BooleanConfig("Extend", "Extends surround if the player is not in the center of a block", false, () -> !centerConfig.getValue());
    Config<Boolean> supportConfig = new BooleanConfig("Support", "Creates a floor for the surround if there is none", true);
    Config<Integer> shiftTicksConfig = new NumberConfig<>("ShiftTicks", "The number of blocks to place per tick", 1, 2, 5);
    Config<Integer> shiftDelayConfig = new NumberConfig<>("ShiftDelay", "The delay between each block placement interval", 0, 1, 5);
    Config<Boolean> jumpDisableConfig = new BooleanConfig("AutoDisable", "Disables after moving out of the hole", false);

    private final Map<BlockPos, Animation> fadeList = new HashMap<>();

    private List<BlockPos> surround = new ArrayList<>();
    private List<BlockPos> placements = new ArrayList<>();

    private int blocksPlaced;
    private int shiftDelay;

    private double prevY;

    public SurroundRewriteModule() {
        super("SurroundRewrite", "Surrounds feet with obsidian by chronos rewritten", ModuleCategory.COMBAT, 950);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) {
            return;
        }
        if (centerConfig.getValue()) {
            double x = Math.floor(mc.player.getX()) + 0.5;
            double z = Math.floor(mc.player.getZ()) + 0.5;
            Managers.MOVEMENT.setMotionXZ((x - mc.player.getX()) / 2.0, (z - mc.player.getZ()) / 2.0);
        }
        prevY = mc.player.getY();
    }

    @EventListener
    public void onDisconnect(DisconnectEvent event) {
        disable();
    }

    @EventListener
    public void onRemoveEntity(RemoveEntityEvent event) {
        if (event.getEntity() == mc.player) {
            disable();
        }
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event) {
        if (Modules.SELF_TRAP.isEnabled()) {
            return;
        }
        // Do we need this check?? Surround is always highest prio
        blocksPlaced = 0;
        if (jumpDisableConfig.getValue() && Math.abs(mc.player.getY() - prevY) > 0.5) {
            disable();
            return;
        }
        BlockPos pos = PlayerUtil.getRoundedBlockPos(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        if (shiftDelay < shiftDelayConfig.getValue()) {
            shiftDelay++;
            return;
        }

        // Don't calculate surround positions if no resistant block is in your hotbar
        final int slot = getResistantBlockItem();
        if (slot == -1) {
            return;
        }

        surround = getSurroundPositions(pos);
        placements = surround.stream().filter(blockPos -> mc.world.getBlockState(blockPos).isReplaceable()).collect(Collectors.toList());
        // We should not be doing anything if we have nothing to place
        if (placements.isEmpty()) {
            return;
        }
        if (supportConfig.getValue()) {
            for (BlockPos block : new ArrayList<>(placements)) {
                Direction direction = Managers.INTERACT.getInteractDirection(block, grimConfig.getValue(), strictDirectionConfig.getValue());
                if (direction == null) {
                    placements.add(block.down());
                }
            }
        }
        Collections.reverse(placements);
        final int shiftTicks = shiftTicksConfig.getValue();
        while (blocksPlaced < shiftTicks && !placements.isEmpty()) {
            if (blocksPlaced >= placements.size()) {
                break;
            }
            BlockPos targetPos = placements.get(blocksPlaced);
            blocksPlaced++;
            shiftDelay = 0;
            // All rotations for shift ticks must send extra packet
            // This may not work on all servers
            attackPlace(targetPos, slot);
        }
    }

    private void attack(Entity entity) {
        Managers.NETWORK.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking()));
        Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
    }

    private void attackPlace(BlockPos targetPos) {
        final int slot = getResistantBlockItem();
        if (slot == -1) {
            return;
        }
        attackPlace(targetPos, slot);
    }

    private void attackPlace(BlockPos targetPos, int slot) {
        if (attackConfig.getValue()) {
            List<Entity> entities = mc.world.getOtherEntities(null, new Box(targetPos)).stream().filter(e -> e instanceof EndCrystalEntity).toList();
            for (Entity entity : entities) {
                attack(entity);
            }
        }

        Managers.INTERACT.placeBlock(targetPos, slot, grimConfig.getValue(), strictDirectionConfig.getValue(), false, (state, angles) ->
        {
            if (rotateConfig.getValue()) {
                if (state) {
                    Managers.ROTATION.setRotationSilent(angles[0], angles[1], grimConfig.getValue());
                    setRotation(angles[0], angles[1]);
                } else {
                    Managers.ROTATION.setRotationSilentSync(grimConfig.getValue());
                }
            }
        });
    }

    public List<BlockPos> getSurroundPositions(BlockPos pos) {
        List<BlockPos> entities = getSurroundEntities(pos);
        List<BlockPos> blocks = new CopyOnWriteArrayList<>();
        for (BlockPos epos : entities) {
            for (Direction dir2 : Direction.values()) {
                if (!dir2.getAxis().isHorizontal()) {
                    continue;
                }
                BlockPos pos2 = epos.add(dir2.getVector());
                if (entities.contains(pos2) || blocks.contains(pos2)) {
                    continue;
                }
                double dist = mc.player.squaredDistanceTo(pos2.toCenterPos());
                if (dist > ((NumberConfig) placeRangeConfig).getValueSq()) {
                    continue;
                }
                blocks.add(pos2);
            }
        }
        for (BlockPos entityPos : entities) {
            if (entityPos == pos) {
                continue;
            }
            blocks.add(entityPos.down());
        }
        return blocks;
    }

    public List<BlockPos> getSurroundEntities(Entity entity) {
        List<BlockPos> entities = new LinkedList<>();
        entities.add(entity.getBlockPos());
        if (extendConfig.getValue()) {
            for (Direction dir : Direction.values()) {
                if (!dir.getAxis().isHorizontal()) {
                    continue;
                }
                entities.addAll(PositionUtil.getAllInBox(entity.getBoundingBox(), entity.getBlockPos()));
            }
        }
        return entities;
    }

    public List<BlockPos> getSurroundEntities(BlockPos pos) {
        List<BlockPos> entities = new LinkedList<>();
        entities.add(pos);
        if (extendConfig.getValue()) {
            for (Direction dir : Direction.values()) {
                if (!dir.getAxis().isHorizontal()) {
                    continue;
                }
                BlockPos pos1 = pos.add(dir.getVector());
                List<Entity> box = mc.world.getOtherEntities(null, new Box(pos1))
                        .stream().filter(e -> !isEntityBlockingSurround(e)).toList();
                if (box.isEmpty()) {
                    continue;
                }
                for (Entity entity : box) {
                    entities.addAll(PositionUtil.getAllInBox(entity.getBoundingBox(), pos));
                }
            }
        }
        return entities;
    }

    public List<BlockPos> getEntitySurroundNoSupport(Entity entity) {
        List<BlockPos> entities = getSurroundEntities(entity);
        List<BlockPos> blocks = new CopyOnWriteArrayList<>();
        for (BlockPos epos : entities) {
            for (Direction dir2 : Direction.values()) {
                if (!dir2.getAxis().isHorizontal()) {
                    continue;
                }
                BlockPos pos2 = epos.add(dir2.getVector());
                if (entities.contains(pos2) || blocks.contains(pos2)) {
                    continue;
                }
                double dist = mc.player.squaredDistanceTo(pos2.toCenterPos());
                if (dist > ((NumberConfig) placeRangeConfig).getValueSq()) {
                    continue;
                }
                blocks.add(pos2);
            }
        }
        return blocks;
    }

    public boolean isEntityBlockingSurround(Entity entity) {
        return entity instanceof ItemEntity || entity instanceof ExperienceOrbEntity
                || (entity instanceof EndCrystalEntity && attackConfig.getValue());
    }

    @EventListener
    public void onAddEntity(AddEntityEvent event) {
        if (!(event.getEntity() instanceof EndCrystalEntity crystalEntity) || !attackConfig.getValue() || Modules.SELF_TRAP.isEnabled()) {
            return;
        }
        for (BlockPos blockPos : surround) {
            if (crystalEntity.getBlockPos() == blockPos) {
                Managers.NETWORK.sendPacket(PlayerInteractEntityC2SPacket.attack(crystalEntity, mc.player.isSneaking()));
                Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                break;
            }
        }
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event) {
        if (mc.player == null || Modules.SELF_TRAP.isEnabled()) {
            return;
        }
        if (event.getPacket() instanceof BlockUpdateS2CPacket packet) {
            final BlockState state = packet.getState();
            final BlockPos targetPos = packet.getPos();
            if (surround.contains(targetPos) && state.isReplaceable()) {
                blocksPlaced++;
                RenderSystem.recordRenderCall(() -> attackPlace(targetPos));
            }
        } else if (event.getPacket() instanceof PlaySoundS2CPacket packet
                && packet.getCategory() == SoundCategory.BLOCKS
                && packet.getSound().value() == SoundEvents.ENTITY_GENERIC_EXPLODE) {
            BlockPos targetPos = BlockPos.ofFloored(packet.getX(), packet.getY(), packet.getZ());
            if (surround.contains(targetPos)) {
                blocksPlaced++;
                RenderSystem.recordRenderCall(() -> attackPlace(targetPos));
            }
        }
    }
}
