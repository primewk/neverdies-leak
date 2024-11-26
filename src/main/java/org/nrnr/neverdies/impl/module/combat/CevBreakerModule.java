package org.nrnr.neverdies.impl.module.combat;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongBidirectionalIterator;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.config.setting.EnumConfig;
import org.nrnr.neverdies.api.config.setting.NumberConfig;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.impl.event.PacketEvent;
import org.nrnr.neverdies.impl.event.RunTickEvent;
import org.nrnr.neverdies.impl.event.network.PlayerTickEvent;
import org.nrnr.neverdies.impl.manager.player.rotation.Rotation;
import org.nrnr.neverdies.init.Managers;
import org.nrnr.neverdies.init.Modules;
import org.nrnr.neverdies.mixin.accessor.EntityTrackingSectionAccessor;
import org.nrnr.neverdies.mixin.accessor.SectionedEntityCacheAccessor;
import org.nrnr.neverdies.mixin.accessor.SimpleEntityLookupAccessor;
import org.nrnr.neverdies.mixin.accessor.WorldAccessor;
import org.nrnr.neverdies.util.chat.ChatUtil;
import org.nrnr.neverdies.util.player.FindItemResult;
import org.nrnr.neverdies.util.player.InventoryUtil;
import org.nrnr.neverdies.util.player.PlayerUtil;
import org.nrnr.neverdies.util.player.RotationUtil;
import org.nrnr.neverdies.util.world.EndCrystalUtil;
import org.nrnr.neverdies.util.world.PathUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EndCrystalItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.entity.EntityLookup;
import net.minecraft.world.entity.EntityTrackingSection;
import net.minecraft.world.entity.SectionedEntityCache;
import net.minecraft.world.entity.SimpleEntityLookup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public class CevBreakerModule extends ToggleModule {
    public enum Mode {
        Packet,
        Bypass,
        Instant
    }

    public Config<Mode> mode = new EnumConfig<>("mode", "The Cev Mode", Mode.Packet, Mode.values());
    Config<Boolean> rotateConfig = new BooleanConfig("Rotate", "Rotates to block before placing", false);
    Config<Integer> delay = new NumberConfig<>("Delay", "The delay between each block placement interval", 0, 1, 10, () -> mode.getValue() == Mode.Instant);
    Config<Integer> breakDamage = new NumberConfig<>("breakDamage", "The delay between each block placement interval", 0, 7, 10);
    Config<Boolean> pauseOnEat = new BooleanConfig("pauseOnEat", "Rotates to block before placing", false);
    Config<Boolean> airPlace = new BooleanConfig("airPlace", "Rotates to block before placing", false);
    Config<Boolean> swapBack = new BooleanConfig("swapBack", "Rotates to block before placing", false, () -> mode.getValue() != Mode.Bypass);
    Config<Boolean> trap = new BooleanConfig("trap", "Rotates to block before placing", false);
    Config<Boolean> strictDirectionConfig = new BooleanConfig("StrictDirection", "Places on visible sides only", false);
    Config<Boolean> grimConfig = new BooleanConfig("Grim", "Places using grim instant rotations", false);

    public CevBreakerModule() {
        super("CevBreaker", "Places obsidian above players head and places/breaks crystals", ModuleCategory.COMBAT);
    }

    private PlayerEntity target;
    private BlockPos targetPos;
    private BlockPos mineTarget;
    private boolean startedYet, speedMine;
    private int switchDelayLeft;
    private int delayLeft;
    private int max;
    private int lastSlot;
    private int stage;
    private final List<PlayerEntity> blacklisted = new ArrayList();
    private final List<EndCrystalEntity> crystals = new ArrayList();
    public static Vec3i[] CITY_WITHOUT_BURROW = new Vec3i[]{
            new Vec3i(1, 0, 0), new Vec3i(0, 0, 1), new Vec3i(-1, 0, 0), new Vec3i(0, 0, -1)
    };

    @Override
    public void onEnable() {
        this.target = null;
        this.targetPos = null;
        this.mineTarget = null;
        this.startedYet = false;
        this.speedMine = false;
        this.switchDelayLeft = 0;
        this.delayLeft = 0;
        this.max = 0;
        this.lastSlot = 0;
        this.stage = 0;
        this.blacklisted.clear();
        if (this.mc.player != null) {
            this.badTarget(this.target, "No target found, disabling...");
        }
    }

    public BlockPos getBreakingPos() {
        BlockPos a = this.targetPos.add(0, (int) Math.ceil((double)this.target.getEyeHeight(this.target.getPose())), 0);

        if ((mc.world.getBlockState(a).isReplaceable() || mc.world.getBlockState(a).getBlock() == Blocks.OBSIDIAN) && mc.world.getBlockState(a.up()).isAir()) return a;
        else {
            List<BlockPos> pos = new ArrayList<>();

            for (Vec3i i : CITY_WITHOUT_BURROW) {
                BlockPos b = this.targetPos.add(i).up();

                if ((mc.world.getBlockState(b).isReplaceable() || mc.world.getBlockState(b).getBlock() == Blocks.OBSIDIAN) && mc.world.getBlockState(b.up()).isAir())
                    pos.add(b);
            }

            pos.sort(Comparator.comparingDouble(PlayerUtil::distanceTo));

            if (!pos.isEmpty()) return pos.get(0);
        }

        return a;
    }

    @EventListener
    public void onTick(RunTickEvent event) {
        if (mc.player == null || mc.world == null) return;

        --this.switchDelayLeft;
        --this.delayLeft;
        Item mItem = this.mc.player.getMainHandStack().getItem();
        Item oItem = this.mc.player.getOffHandStack().getItem();
        if (!this.pauseOnEat.getValue() || !this.mc.player.isUsingItem() || !mItem.isFood() && !oItem.isFood()) {
            if (this.noEntities(null)) {
                this.toggle();
            } else if (!this.target.isDead() && !(this.target.distanceTo(this.mc.player) > 6.0F)) {
                int crystalSlot = InventoryUtil.findInHotbar(new Item[]{Items.END_CRYSTAL}).slot();
                FindItemResult obsidianResult = InventoryUtil.findInHotbar(new Item[]{Items.OBSIDIAN});
                int pickSlot = InventoryUtil.findInHotbar(itemStack -> itemStack.getItem() instanceof PickaxeItem).slot();
                int slot = this.mc.player.getInventory().selectedSlot;
                if (slot != crystalSlot && slot != obsidianResult.slot() && slot != pickSlot) {
                    this.lastSlot = slot;
                }

                this.mineTarget = getBreakingPos();
                boolean crystalThere = false;

                for(EndCrystalEntity crystal : this.crystals) {
                    if (crystal.getBlockPos().add(0, -1, 0).equals(this.mineTarget)) {
                        crystalThere = true;
                        break;
                    }
                }

                String errorMsg = crystalSlot == -1 && !crystalThere && !(oItem instanceof EndCrystalItem)
                        ? "crystals"
                        : (obsidianResult.slot() == -1 ? "obsidian" : (pickSlot == -1 ? "pickaxe" : ""));
                if (!errorMsg.isEmpty()) {
                    this.toggle();
                } else {
                    BlockState blockState = this.mc.world.getBlockState(this.mineTarget);
                    float[] rotation = calculateAngle(
                            new Vec3d(
                                    (double)this.mineTarget.getX() + 0.5, (double)this.mineTarget.getY() + 0.5, (double)this.mineTarget.getZ() + 0.5
                            )
                    );
                    if (!blockState.isOf(Blocks.OBSIDIAN) && !crystalThere && (mItem.equals(Items.OBSIDIAN) || this.switchDelayLeft <= 0)) {
                        FindItemResult result = InventoryUtil.findInHotbar(new Item[]{Items.OBSIDIAN});
                        if (this.airPlace.getValue()) {
                            if (!Managers.INTERACT.placeBlock(mineTarget, result.slot(), strictDirectionConfig.getValue(), true, (state, angles) ->
                            {
                                if (rotateConfig.getValue())
                                {
                                    if (state)
                                    {
                                        Managers.ROTATION.setRotationSilent(angles[0], angles[1], grimConfig.getValue());
                                    }
                                    else
                                    {
                                        Managers.ROTATION.setRotationSilentSync(grimConfig.getValue());
                                    }
                                }
                            })) {
                                this.badTarget(this.target, "Can't place obsidian above the target! Disabling...");
                                return;
                            }
                        } else {
                            List<BlockPos> list = new ArrayList();
                            boolean couldPlace = false;
                            if (this.findNeighbour(list, this.mineTarget, 0)) {
                                this.add(list, this.mineTarget);

                                for(BlockPos pos : list) {
                                    if (Managers.INTERACT.placeBlock(pos, result.slot(), strictDirectionConfig.getValue(), true, (state, angles) ->
                                    {
                                        if (rotateConfig.getValue())
                                        {
                                            if (state)
                                            {
                                                Managers.ROTATION.setRotationSilent(angles[0], angles[1], grimConfig.getValue());
                                            }
                                            else
                                            {
                                                Managers.ROTATION.setRotationSilentSync(grimConfig.getValue());
                                            }
                                        }
                                    })) {
                                        couldPlace = true;
                                        this.stage = 1;
                                    }
                                }

                                if (!couldPlace) {
                                    this.badTarget(this.target, "Can't place obsidian above the target! Disabling...");
                                    return;
                                }
                            }
                        }
                    }

                    boolean offhand = oItem instanceof EndCrystalItem;
                    boolean mainhand = mItem instanceof EndCrystalItem;
                    if (!crystalThere && blockState.isOf(Blocks.OBSIDIAN)) {
                        if (!offhand && !mainhand && this.switchDelayLeft > 0) {
                            return;
                        }

                        double x = (double)this.mineTarget.up().getX();
                        double y = (double)this.mineTarget.up().getY();
                        double z = (double)this.mineTarget.up().getZ();
                        if (intersectsWithEntity(new Box(x, y, z, x + 1.0, y + 2.0, z + 1.0), entity -> true)) {
                            this.badTarget(this.target, "Can't place the crystal, there are entities in its way! Disabling...");
                            return;
                        }

                        if (!this.mc.world.getBlockState(this.mineTarget.up()).isAir()) {
                            this.badTarget(this.target, "Can't place the crystal, there is not enough space! Disabling...");
                            return;
                        }

                        if (!offhand && !mainhand) {
                            this.mc.player.getInventory().selectedSlot = crystalSlot;
                        }

                        Hand hand = offhand ? Hand.OFF_HAND : Hand.MAIN_HAND;
                        Direction sidePlace = getPlaceDirection(mineTarget);

                        BlockHitResult result = new BlockHitResult(mineTarget.toCenterPos(), sidePlace, mineTarget, false);

                        if (this.rotateConfig.getValue()) {
                            setRotation(rotation[0], rotation[1]);
                            this.mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(hand, result, 0));
                        } else {
                            this.mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(hand, result, 0));
                        }

                        mc.player.swingHand(hand);
                        this.stage = 2;
                    }

                    if (this.trap.getValue() && (mItem.equals(Items.OBSIDIAN) || this.switchDelayLeft <= 0)) {
                        for(Vec3i offset : CITY_WITHOUT_BURROW) {
                            BlockPos trapPos = this.targetPos.add(offset).up();
                            if (this.mc.world.getBlockState(trapPos).isReplaceable()
                                    && this.mc.world.canPlace(Blocks.OBSIDIAN.getDefaultState(), trapPos, ShapeContext.absent())) {
                                Managers.INTERACT.placeBlock(trapPos, obsidianResult.slot(), strictDirectionConfig.getValue(), true, (state, angles) ->
                                {
                                    if (rotateConfig.getValue())
                                    {
                                        if (state)
                                        {
                                            Managers.ROTATION.setRotationSilent(angles[0], angles[1], grimConfig.getValue());
                                        }
                                        else
                                        {
                                            Managers.ROTATION.setRotationSilentSync(grimConfig.getValue());
                                        }
                                    }
                                });
                            }
                        }
                    }

                    if (this.swapBack.getValue()
                            && this.switchDelayLeft <= 0
                            && !blockState.isAir()
                            && this.mode.getValue() != Mode.Bypass
                            && this.delayLeft > 2
                            && this.switchDelayLeft <= 0) {
                        this.mc.player.getInventory().selectedSlot = this.lastSlot;
                    }

                    if ((this.mc.player.getInventory().selectedSlot == pickSlot || this.switchDelayLeft <= 0)
                            && crystalThere
                            && blockState.isOf(Blocks.OBSIDIAN)) {
                        Direction direction = rayTraceCheck(this.mineTarget, true);
                        this.max = getBlockBreakingSpeed(blockState, this.mineTarget, pickSlot);
                        switch((Mode)this.mode.getValue()) {
                            case Packet:
                                if (Modules.PACKETMINE.isEnabled()) {
                                    if (!speedMine) {
                                        mc.interactionManager.attackBlock(mineTarget, direction);
                                        speedMine = true;
                                    }
                                } else {
                                    if (this.startedYet && !blockState.isAir() && (!this.startedYet || this.delayLeft > -2)) {
                                        if (this.delayLeft <= 0) {
                                            this.mc.player.getInventory().selectedSlot = pickSlot;
                                        }
                                    } else {
                                        this.delayLeft = this.max;
                                        mine(this.mineTarget, this.rotateConfig.getValue());
                                        this.startedYet = true;
                                    }
                                }
                                break;
                            case Instant:
                                if (!this.startedYet) {
                                    if (this.rotateConfig.getValue()) {
                                        setRotation(rotation[0], rotation[1]);
                                        this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, this.mineTarget, direction));
                                    } else {
                                        this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, this.mineTarget, direction));
                                    }

                                    mc.player.swingHand(Hand.MAIN_HAND);
                                    this.delayLeft = this.max;
                                    this.startedYet = true;
                                } else if (this.delayLeft <= 0) {
                                    this.mc.player.getInventory().selectedSlot = pickSlot;
                                    if (this.rotateConfig.getValue()) {
                                        setRotation(rotation[0], rotation[1]);
                                        this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, this.mineTarget, direction));
                                    } else {
                                        this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, this.mineTarget, direction));
                                    }

                                    mc.player.swingHand(Hand.MAIN_HAND);
                                    this.delayLeft = this.delay.getValue();
                                }
                                break;
                            default:
                                if (Modules.PACKETMINE.isEnabled()) {
                                    if (!speedMine) {
                                        mc.interactionManager.attackBlock(mineTarget, direction);
                                        speedMine = true;
                                    }
                                } else {
                                    this.mc.player.getInventory().selectedSlot = pickSlot;
                                    if (this.rotateConfig.getValue()) {
                                        setRotation(rotation[0], rotation[1]);
                                        this.mc.interactionManager.updateBlockBreakingProgress(this.mineTarget, direction);
                                    } else {
                                        this.mc.interactionManager.updateBlockBreakingProgress(this.mineTarget, direction);
                                    }

                                    mc.player.swingHand(Hand.MAIN_HAND);
                                    if (!this.startedYet || blockState.isAir() || this.startedYet && this.delayLeft <= -2) {
                                        this.startedYet = true;
                                        this.delayLeft = this.max;
                                    }
                                }
                        }
                    }

                    if (blockState.isAir()) {
                        speedMine = false;
                        if (crystalThere) {
                            this.stage = 3;
                        } else {
                            this.stage = 0;
                        }
                    }

                    for(EndCrystalEntity crystal : this.crystals) {
                        if (EndCrystalUtil.getDamageTo(this.target, crystal.getPos()) >= breakDamage.getValue()) {
                            float[] breakRotation = calculateAngle(crystal.getEyePos());
                            if (this.rotateConfig.getValue()) {
                                setRotation(breakRotation[0], breakRotation[1]);
                                this.mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(crystal, false));
                            } else {
                                this.mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(crystal, this.mc.player.isSneaking()));
                            }

                            mc.player.swingHand(Hand.MAIN_HAND);
                            this.stage = 0;
                            break;
                        }
                    }
                }
            }
        } else {
            if (this.delayLeft < 0) {
                ++this.delayLeft;
            }
        }
    }

    private Direction getPlaceDirection(BlockPos blockPos) {
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
                if (mc.world.isAir(blockPos.offset(direction))) {
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
        return Direction.UP;
    }

    public static boolean intersectsWithEntity(Box box, Predicate<Entity> predicate) {
        EntityLookup<Entity> entityLookup = ((WorldAccessor) mc.world).getEntityLookup();

        // Fast implementation using SimpleEntityLookup that returns on the first intersecting entity
        if (entityLookup instanceof SimpleEntityLookup<Entity> simpleEntityLookup) {
            SectionedEntityCache<Entity> cache = ((SimpleEntityLookupAccessor) simpleEntityLookup).getCache();
            LongSortedSet trackedPositions = ((SectionedEntityCacheAccessor) cache).getTrackedPositions();
            Long2ObjectMap<EntityTrackingSection<Entity>> trackingSections = ((SectionedEntityCacheAccessor) cache).getTrackingSections();

            int i = ChunkSectionPos.getSectionCoord(box.minX - 2);
            int j = ChunkSectionPos.getSectionCoord(box.minY - 2);
            int k = ChunkSectionPos.getSectionCoord(box.minZ - 2);
            int l = ChunkSectionPos.getSectionCoord(box.maxX + 2);
            int m = ChunkSectionPos.getSectionCoord(box.maxY + 2);
            int n = ChunkSectionPos.getSectionCoord(box.maxZ + 2);

            for (int o = i; o <= l; o++) {
                long p = ChunkSectionPos.asLong(o, 0, 0);
                long q = ChunkSectionPos.asLong(o, -1, -1);
                LongBidirectionalIterator longIterator = trackedPositions.subSet(p, q + 1).iterator();

                while (longIterator.hasNext()) {
                    long r = longIterator.nextLong();
                    int s = ChunkSectionPos.unpackY(r);
                    int t = ChunkSectionPos.unpackZ(r);

                    if (s >= j && s <= m && t >= k && t <= n) {
                        EntityTrackingSection<Entity> entityTrackingSection = trackingSections.get(r);

                        if (entityTrackingSection != null && entityTrackingSection.getStatus().shouldTrack()) {
                            for (Entity entity : ((EntityTrackingSectionAccessor) entityTrackingSection).<Entity>getCollection()) {
                                if (entity.getBoundingBox().intersects(box) && predicate.test(entity)) return true;
                            }
                        }
                    }
                }
            }

            return false;
        }

        // Slow implementation that loops every entity if for some reason the EntityLookup implementation is changed
        AtomicBoolean found = new AtomicBoolean(false);

        entityLookup.forEachIntersects(box, entity -> {
            if (!found.get() && predicate.test(entity)) found.set(true);
        });

        return found.get();
    }

    public static int getBlockBreakingSpeed(BlockState block, BlockPos pos, int slot) {
        ClientPlayerEntity player = mc.player;
        float f = player.getInventory().getStack(slot).getMiningSpeedMultiplier(block);
        if (f > 1.0F) {
            int i = EnchantmentHelper.get(player.getInventory().getStack(slot)).getOrDefault(Enchantments.EFFICIENCY, 0);
            if (i > 0) {
                f += (float)(i * i + 1);
            }
        }

        if (StatusEffectUtil.hasHaste(player)) {
            f *= 1.0F + (float)(StatusEffectUtil.getHasteAmplifier(player) + 1) * 0.2F;
        }

        if (player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            float k = switch(player.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) {
                case 0 -> 0.3F;
                case 1 -> 0.09F;
                case 2 -> 0.0027F;
                default -> 8.1E-4F;
            };
            f *= k;
        }

        if (player.isSubmergedIn(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(player)) {
            f /= 5.0F;
        }

        if (!player.isOnGround()) {
            f /= 5.0F;
        }

        float t = block.getHardness(mc.world, pos);
        return t == -1.0F ? 0 : (int)Math.ceil((double)(1.0F / (f / t / 30.0F)));
    }

    public static Direction rayTraceCheck(BlockPos pos, boolean forceReturn) {
        Vec3d eyesPos = mc.player.getEyePos();

        for(Direction direction : Direction.values()) {
            RaycastContext raycastContext = new RaycastContext(
                    eyesPos, sideVec(pos, direction), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player
            );
            BlockHitResult result = mc.world.raycast(raycastContext);
            if (result != null && result.getType() == HitResult.Type.BLOCK && result.getBlockPos().equals(pos)) {
                return direction;
            }
        }

        if (!forceReturn) {
            return null;
        } else {
            return (double)pos.getY() > eyesPos.y ? Direction.DOWN : Direction.UP;
        }
    }

    public static Vec3d sideVec(BlockPos pos, Direction direction) {
        return Vec3d.ofCenter(pos)
                .add((double)direction.getOffsetX() * 0.5, (double)direction.getOffsetY() * 0.5, (double)direction.getOffsetZ() * 0.5);
    }

    public void setRotation(float yaw, float pitch)
    {
        Managers.ROTATION.setRotation(new Rotation(50, yaw, pitch));
    }

    public void mine(BlockPos blockPos, boolean rotate) {
        if (rotate) {
            float[] rotations = RotationUtil.getRotationsTo(mc.player.getEyePos(), blockPos.toCenterPos());
            setRotation(rotations[0], rotations[1]);
            mine(blockPos, false);
        } else {
            mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, Direction.UP));
            mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, Direction.UP));
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    public static float[] calculateAngle(Vec3d target) {
        Vec3d eyesPos = new Vec3d(mc.player.getX(), mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()), mc.player.getZ());

        double dX = target.x - eyesPos.x;
        double dY = (target.y - eyesPos.y) * -1.0D;
        double dZ = target.z - eyesPos.z;

        double dist = Math.sqrt(dX * dX + dZ * dZ);

        return new float[]{(float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(dZ, dX)) - 90.0D), (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(dY, dist)))};
    }

    private void add(List<BlockPos> list, BlockPos pos) {
        if (!list.contains(pos)) {
            if (this.mc.world.getBlockState(pos).isReplaceable()) {
                if (this.airPlace.getValue()) {
                    list.add(pos);
                } else if (this.findNeighbour(list, pos, 0)) {
                    list.add(pos);
                }
            }
        }
    }

    private boolean findNeighbour(List<BlockPos> list, BlockPos pos, int iteration) {
        if (iteration > 3) {
            return false;
        } else {
            BlockState placeState = Blocks.OBSIDIAN.getDefaultState();

            for(Direction direction : Direction.values()) {
                if (this.mc.world.canPlace(placeState, pos.offset(direction), ShapeContext.absent())
                        && (list.contains(pos.offset(direction)) || !this.mc.world.getBlockState(pos.offset(direction)).isReplaceable())) {
                    return true;
                }
            }

            for(Direction direction : Direction.values()) {
                if (this.mc.world.canPlace(placeState, pos.offset(direction), ShapeContext.absent())
                        && this.findNeighbour(list, pos.offset(direction), iteration + 1)) {
                    PathUtils.smartAdd(list, pos.offset(direction));
                    return true;
                }
            }

            return false;
        }
    }

    @EventListener
    private void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof UpdateSelectedSlotC2SPacket) {
            this.switchDelayLeft = 1;
        }
    }

    private void badTarget(PlayerEntity target, String message) {
        if (this.noEntities(target)) {
            this.toggle();
        } else {
            ChatUtil.clientSendMessage("Cev crystalling " + this.target.getGameProfile().getName() + "...", new Object[0]);
        }
    }

    private boolean noEntities(PlayerEntity blacklist) {
        this.crystals.clear();
        if (blacklist != null) {
            this.blacklisted.add(blacklist);
        }

        if (this.blacklisted.contains(this.target)) {
            this.target = null;
        }

        for(Entity entity : this.mc.world.getEntities()) {
            if (entity.isInRange(this.mc.player, 6.0) && entity.isAlive() && entity != this.mc.player) {
                if (entity instanceof OtherClientPlayerEntity) {
                    if (!this.blacklisted.contains(entity)
                            && !Managers.SOCIAL.isFriend(((PlayerEntity)entity).getName())
                            && (this.target == null || this.mc.player.distanceTo(entity) < this.mc.player.distanceTo(this.target))) {
                        this.target = (PlayerEntity)entity;
                        this.targetPos = this.target.getBlockPos();
                    }
                } else if (entity instanceof EndCrystalEntity) {
                    this.crystals.add((EndCrystalEntity)entity);
                }
            }
        }

        return this.target == null;
    }

   // @Override
    public void onUpdate() {
    }
}