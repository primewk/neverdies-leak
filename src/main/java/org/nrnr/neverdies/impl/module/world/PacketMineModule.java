package org.nrnr.neverdies.impl.module.world;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.config.setting.EnumConfig;
import org.nrnr.neverdies.api.config.setting.NumberConfig;
import org.nrnr.neverdies.api.event.EventStage;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.RotationModule;
import org.nrnr.neverdies.api.render.RenderManager;
import org.nrnr.neverdies.impl.event.TickEvent;
import org.nrnr.neverdies.impl.event.network.AttackBlockEvent;
import org.nrnr.neverdies.impl.event.network.PacketEvent;
import org.nrnr.neverdies.impl.event.network.PlayerTickEvent;
import org.nrnr.neverdies.impl.event.render.RenderWorldEvent;
import org.nrnr.neverdies.init.Managers;
import org.nrnr.neverdies.init.Modules;
import org.nrnr.neverdies.mixin.accessor.AccessorClientPlayerInteractionManager;
import org.nrnr.neverdies.util.player.FindItemResult;
import org.nrnr.neverdies.util.player.InventoryUtil;
import org.nrnr.neverdies.util.player.RotationUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;

import static org.nrnr.neverdies.util.player.InventoryUtil.findInHotbar;

/**
 * @author chronos
 * @since 1.0
 */
public class PacketMineModule extends RotationModule {

    Config<SpeedmineMode> modeConfig = new EnumConfig<>("Mode", "The mining mode for speedmine", SpeedmineMode.PACKET, SpeedmineMode.values());
    Config<Float> mineSpeedConfig = new NumberConfig<>("Speed", "The speed to mine blocks", 0.0f, 0.7f, 0.9f, () -> modeConfig.getValue() == SpeedmineMode.DAMAGE);
    Config<Boolean> instantConfig = new BooleanConfig("Instant", "Instantly removes the mining block", false, () -> modeConfig.getValue() == SpeedmineMode.PACKET);
    Config<Float> rangeConfig = new NumberConfig<>("Range", "Range for mine", 1.0f, 4.5f, 6.0f, () -> modeConfig.getValue() == SpeedmineMode.PACKET);
    Config<Swap> swapConfig = new EnumConfig<>("AutoSwap", "Swaps to the best tool once the mining is complete", Swap.SILENT, Swap.values(), () -> modeConfig.getValue() == SpeedmineMode.PACKET);
    Config<Boolean> bomber = new BooleanConfig("Bomber", "Mines block above head", false);
    Config<Float> enemyRangeConfig = new NumberConfig<>("EnemyRange", "Range to search for targets", 1.0f, 5.0f, 10.0f, () -> bomber.getValue());
    Config<BomberSwap> autoSwapConfig = new EnumConfig<>("Bomber-Swap", "Swaps to an end crystal before placing if the player is not holding one", BomberSwap.NORMAL    ,BomberSwap.values(), () -> bomber.getValue());
    Config<Boolean> remineConfig = new BooleanConfig("Remine", "Remines the block", true, () -> modeConfig.getValue() == SpeedmineMode.PACKET);
    Config<Boolean> rotateConfig = new BooleanConfig("Rotate", "Rotates when mining the block", true, () -> modeConfig.getValue() == SpeedmineMode.PACKET);
    Config<Boolean> multitaskConfig = new BooleanConfig("Multitask", "You can Eat While Mining", true);
    Config<Boolean> grimConfig = new BooleanConfig("Grim", "Uses grim block breaking speeds", false);
    private BlockPos mining;
    private BlockState state;
    private Direction direction;
    public float damage;
    private boolean switchBack;



    public PacketMineModule() {
        super("PacketMine", "Mines faster", ModuleCategory.WORLD, 900);
    }

    @Override
    public String getModuleData() {
        DecimalFormat decimal = new DecimalFormat("0.0");
        return decimal.format(damage);
    }

    @Override
    public void onDisable() {
        if (mining != null) {
            Managers.INVENTORY.syncToClient();
        }
        mining = null;
        state = null;
        direction = null;
        damage = 0.0f;
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() != EventStage.PRE || modeConfig.getValue() != SpeedmineMode.DAMAGE) {
            return;
        }
        AccessorClientPlayerInteractionManager interactionManager =
                (AccessorClientPlayerInteractionManager) mc.interactionManager;
        if (interactionManager.hookGetCurrentBreakingProgress() >= mineSpeedConfig.getValue()) {
            interactionManager.hookSetCurrentBreakingProgress(1.0f);
        }
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event) {
        if (modeConfig.getValue() != SpeedmineMode.PACKET || mc.player.isCreative()) {
            return;
        }
        if (mining == null) {
            damage = 0.0f;
            return;
        }
        state = mc.world.getBlockState(mining);
        int prev = mc.player.getInventory().selectedSlot;
        int slot = Modules.AUTO_TOOL.getBestTool(state);
        double dist = mc.player.squaredDistanceTo(mining.toCenterPos());
        if (dist > ((NumberConfig<?>) rangeConfig).getValueSq()
                || state.isAir() || damage > 3.0f) {
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, mining, Direction.DOWN));
            mining = null;
            state = null;
            direction = null;
            damage = 0.0f;
        } else {

             if (damage > 1.0f) //&& !Modules.AUTO_CRYSTAL.isAttacking() && !Modules.AUTO_CRYSTAL.isPlacing() && !mc.player.isUsingItem()) {
             {
                if (isRotationBlocked()) {
                    return;
                }
                if (swapConfig.getValue() != Swap.OFF & swapConfig.getValue() != Swap.ALTERNATE) {
                    Managers.INVENTORY.setSlot(slot);
                    Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, mining, direction));
                    Managers.INVENTORY.syncToClient();
                }
                if (swapConfig.getValue() == Swap.ALTERNATE || slot >= 9) {
                    switchTo(slot, -1);
                    Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, mining, direction));
                    switchTo(prev, slot);
                }

                if (bomber.getValue()) {
                    ArrayList<BlockPos> crystalPositions = new ArrayList<>();
                    BlockPos miningPos = BlockPos.ofFloored(mining.toCenterPos());
                    PlayerEntity playerTarget = null;
                    double minDistance = Float.MAX_VALUE;
                    for (PlayerEntity entity : mc.world.getPlayers()) {
                        if (entity == mc.player || Managers.SOCIAL.isFriend(entity.getName())) {
                            continue;
                        }
                        double dist2 = mc.player.distanceTo(entity);
                        if (dist2 > enemyRangeConfig.getValue()) {
                            continue;
                        }
                        if (dist2 < minDistance) {
                            minDistance = dist2;
                            playerTarget = entity;
                        }
                    }

                    if (!(playerTarget == null)) {

                        BlockPos targetPos = playerTarget.getBlockPos();

                            if (canUseCrystalOnBlock(BlockPos.ofFloored(targetPos.up(2).toCenterPos()))) {
                                crystalPositions.add(BlockPos.ofFloored(targetPos.up(2).toCenterPos()));
                            }

                        BlockPos[] offsets = {
                                new BlockPos(1, -1, 0),   // north
                                new BlockPos(-1, -1, 0),  // south
                                new BlockPos(0, -1, 1),   // east
                                new BlockPos(0, -1, -1)   // west
                        };


                        for (BlockPos offset : offsets) {
                            BlockPos pos = miningPos.add(offset);
                            if (canUseCrystalOnBlock(BlockPos.ofFloored(pos.toCenterPos()))) {
                                crystalPositions.add(BlockPos.ofFloored(pos.toCenterPos()));
                            }
                        }
                    }


                    for (BlockPos pos : crystalPositions) {
                        if (!crystalPositions.isEmpty()) {
                            BlockPos firstPos = crystalPositions.get(0);
                            placeCrystal(firstPos);
                        }
                    }
                }

            } else {
                float delta = calcBlockBreakingDelta(state, mc.world, mining);
                damage += delta;
                if (delta + damage > 1.0f && rotateConfig.getValue()
                        && !Modules.AUTO_CRYSTAL.isAttacking()
                        && !Modules.AUTO_CRYSTAL.isPlacing()) {
                    float[] rotations = RotationUtil.getRotationsTo(mc.player.getEyePos(), mining.toCenterPos());
                    setRotation(rotations[0], rotations[1]);

                }
            }
        }
    }


    /**
     * @param event
     */
    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event) {
        //
        if (event.getPacket() instanceof PlayerActionC2SPacket packet
                && packet.getAction() == PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK
                && modeConfig.getValue() == SpeedmineMode.DAMAGE && grimConfig.getValue()) {
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, packet.getPos().up(500), packet.getDirection()));
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


    private void closeScreen() {
        if (mc.player == null) return;

        sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
    }

    public static void clickSlot(int id) {
        if (id == -1 || mc.interactionManager == null || mc.player == null) return;
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, id, 0, SlotActionType.PICKUP, mc.player);
    }

    public static void clickSlot(int id, SlotActionType type) {
        if (id == -1 || mc.interactionManager == null || mc.player == null) return;
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, id, 0, type, mc.player);
    }

    public static void clickSlot(int id, int button, SlotActionType type) {
        if (id == -1 || mc.interactionManager == null || mc.player == null) return;
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, id, button, type, mc.player);
    }

    public BlockPos getBlockTarget() {
        return mining;
    }

    @EventListener
    public void onAttackBlock(AttackBlockEvent event) {
        if (modeConfig.getValue() != SpeedmineMode.PACKET) {
            return;
        }
        if (mc.player == null || mc.world == null
                || mc.player.isCreative() || mining != null && event.getPos() == mining) {
            return;
        }
        if (mining != null) {
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK,
                    mining, Direction.DOWN));
        }
        mining = event.getPos();
        direction = event.getDirection();
        damage = 0.0f;
        if (mining != null && direction != null) {

            int slot = Modules.AUTO_TOOL.getBestTool(event.getState());
            if (grimConfig.getValue()) {
                Managers.INVENTORY.setSlot(slot);
            }
            event.cancel();
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.START_DESTROY_BLOCK,
                    mining, direction));
            if (grimConfig.getValue()) {
                Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, mining, direction));
            }
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, mining, direction));
            if (instantConfig.getValue()) {
                mc.world.removeBlock(mining, false);
            }
            if (grimConfig.getValue()) {
                Managers.INVENTORY.syncToClient();
            }


        }
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
        if (rotateConfig.getValue()) {
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

    float calcBlockBreakingDelta(BlockState state, BlockView world,
                                 BlockPos pos) {
        if (swapConfig.getValue() == Swap.OFF) {
            return state.calcBlockBreakingDelta(mc.player, mc.world, pos);
        }
        float f = state.getHardness(world, pos);
        if (f == -1.0f) {
            return 0.0f;
        } else {
            int i = canHarvest(state) ? 30 : 100;
            return getBlockBreakingSpeed(state) / f / (float) i;
        }
    }

    private float getBlockBreakingSpeed(BlockState block) {
        int tool = Modules.AUTO_TOOL.getBestTool(block);
        float f = mc.player.getInventory().getStack(tool).getMiningSpeedMultiplier(block);
        if (f > 1.0F) {
            ItemStack stack = mc.player.getInventory().getStack(tool);
            int i = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, stack);
            if (i > 0 && !stack.isEmpty()) {
                f += (float) (i * i + 1);
            }
        }
        if (StatusEffectUtil.hasHaste(mc.player)) {
            f *= 1.0f + (float) (StatusEffectUtil.getHasteAmplifier(mc.player) + 1) * 0.2f;
        }
        if (mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            float g = switch (mc.player.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) {
                case 0 -> 0.3f;
                case 1 -> 0.09f;
                case 2 -> 0.0027f;
                default -> 8.1e-4f;
            };
            f *= g;
        }
        if (mc.player.isSubmergedIn(FluidTags.WATER)
                && !EnchantmentHelper.hasAquaAffinity(mc.player)) {
            f /= 5.0f;
        }
        if (!mc.player.isOnGround()) {
            f /= 5.0f;
        }
        return f;
    }

    private boolean canHarvest(BlockState state) {
        if (state.isToolRequired()) {
            int tool = Modules.AUTO_TOOL.getBestTool(state);
            return mc.player.getInventory().getStack(tool).isSuitableFor(state);
        }
        return true;
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event) {
        if (mining == null || state == null || mc.player.isCreative()
                || modeConfig.getValue() != SpeedmineMode.PACKET) {
            return;
        }
        VoxelShape outlineShape = state.getOutlineShape(mc.world, mining);
        if (outlineShape.isEmpty()) {
            return;
        }
        Box render1 = outlineShape.getBoundingBox();
        Box render = new Box(mining.getX() + render1.minX, mining.getY() + render1.minY,
                mining.getZ() + render1.minZ, mining.getX() + render1.maxX,
                mining.getY() + render1.maxY, mining.getZ() + render1.maxZ);
        Vec3d center = render.getCenter();
        float scale = MathHelper.clamp(damage, 0.0f, 1.0f);
        if (scale > 1.0f) {
            scale = 1.0f;
        }
        double dx = (render1.maxX - render1.minX) / 2.0;
        double dy = (render1.maxY - render1.minY) / 2.0;
        double dz = (render1.maxZ - render1.minZ) / 2.0;
        final Box scaled = new Box(center, center).expand(dx * scale, dy * scale, dz * scale);
        RenderManager.renderBox(event.getMatrices(), scaled,
                damage > 0.95f ? 0x6000ff00 : 0x60ff0000);
        RenderManager.renderBoundingBox(event.getMatrices(), scaled,
                2.5f, damage > 0.95f ? 0x6000ff00 : 0x60ff0000);
    }

    public enum SpeedmineMode {
        PACKET,
        DAMAGE
    }

    public enum Swap {
        NORMAL,
        SILENT,
        ALTERNATE,
        OFF
    }

    public enum BomberSwap {
        NORMAL,
        SILENT,
        SILENT_ALT,
        OFF
    }
}