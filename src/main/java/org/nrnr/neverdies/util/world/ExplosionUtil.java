package org.nrnr.neverdies.util.world;

import com.google.common.collect.Multimap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameMode;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.explosion.Explosion;
import org.nrnr.neverdies.mixin.world.IExplosion;
import org.nrnr.neverdies.mixin.world.IRaycastContext;
import org.nrnr.neverdies.mixin.world.IVec3d;
import org.nrnr.neverdies.util.Globals;

import java.util.function.BiFunction;

import static org.nrnr.neverdies.util.player.PlayerUtil.squaredDistance;

/**
 * @author chronos
 * @since 1.0
 */
public class ExplosionUtil implements Globals {
    /**
     * @param entity
     * @param explosion
     * @return
     */
    public static double getDamageTo(final Entity entity,
                                     final Vec3d explosion) {
        return getDamageTo(entity, explosion, false);
    }

    /**
     * @param entity
     * @param explosion
     * @param ignoreTerrain
     * @return
     */

    private static Explosion explosion;
    public static RaycastContext raycastContext;
    public static RaycastContext bedRaycast;

    public static double bedDamage(LivingEntity player, Box box, Vec3d bed, BlockPos ignore) {
        if (player instanceof PlayerEntity && ((PlayerEntity) player).getAbilities().creativeMode) return 0;

        double modDistance = Math.sqrt(player.squaredDistanceTo(bed));
        if (modDistance > 10) return 0;

        double exposure = getExposure(bed, player, box, raycastContext, ignore, true);
        double impact = (1.0 - (modDistance / 10.0)) * exposure;
        double damage = (impact * impact + impact) / 2 * 7 * (5 * 2) + 1;

        // Multiply damage by difficulty
        damage = getDamageForDifficulty(damage);

        // Reduce by resistance
        damage = resistanceReduction(player, damage);

        // Reduce by armour
        damage = DamageUtil.getDamageLeft((float) damage, (float) player.getArmor(), (float) player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).getValue());

        // Reduce by enchants
        ((IExplosion) explosion).set(bed, 5, true);
        damage = blastProtReduction(player, damage, explosion);

        if (damage < 0) damage = 0;
        return damage;
    }

    public static float bedDamage(LivingEntity target, Vec3d bed) {
        return explosionDamage(target, bed, 10f, false);
    }

    private static float explosionDamage(LivingEntity target, Vec3d explosionPos, float power, boolean predictMovement) {
        return explosionDamage(target, explosionPos, power, predictMovement, HIT_FACTORY);
    }

    private static float explosionDamage(LivingEntity target, Vec3d explosionPos, float power, boolean predictMovement, RaycastFactory raycastFactory) {
        if (target == null) return 0f;
        if (target instanceof PlayerEntity player && EntityUtil.getGameMode(player) == GameMode.CREATIVE && !(player instanceof FakePlayerEntity)) return 0f;

        Vec3d position = predictMovement ? target.getPos().add(target.getVelocity()) : target.getPos();

        Box box = target.getBoundingBox();
        if (predictMovement) box = box.offset(target.getVelocity());

        return explosionDamage(target, position, box, explosionPos, power, raycastFactory);
    }


    public static final RaycastFactory HIT_FACTORY = (context, blockPos) -> {
        BlockState blockState = mc.world.getBlockState(blockPos);
        if (blockState.getBlock().getBlastResistance() < 600) return null;

        return blockState.getCollisionShape(mc.world, blockPos).raycast(context.start(), context.end(), blockPos);
    };


    private static final Vec3d vec3d = new Vec3d(0, 0, 0);

    public static double getExposure(Vec3d source, Entity entity, Box box, RaycastContext raycastContext, BlockPos ignore, boolean ignoreTerrain) {
        double d = 1 / ((box.maxX - box.minX) * 2 + 1);
        double e = 1 / ((box.maxY - box.minY) * 2 + 1);
        double f = 1 / ((box.maxZ - box.minZ) * 2 + 1);
        double g = (1 - Math.floor(1 / d) * d) / 2;
        double h = (1 - Math.floor(1 / f) * f) / 2;

        if (!(d < 0) && !(e < 0) && !(f < 0)) {
            int i = 0;
            int j = 0;

            for (double k = 0; k <= 1; k += d) {
                for (double l = 0; l <= 1; l += e) {
                    for (double m = 0; m <= 1; m += f) {
                        double n = MathHelper.lerp(k, box.minX, box.maxX);
                        double o = MathHelper.lerp(l, box.minY, box.maxY);
                        double p = MathHelper.lerp(m, box.minZ, box.maxZ);

                        ((IVec3d) vec3d).set(n + g, o, p + h);

                        if (raycast(raycastContext, ignore, ignoreTerrain).getType() == HitResult.Type.MISS) i++;

                        j++;
                    }
                }
            }

            return (double) i / j;
        }

        return 0;
    }

    public static float bedDamage(LivingEntity target, Vec3d targetPos, Box targetBox, Vec3d explosionPos, RaycastFactory raycastFactory) {
        return explosionDamage(target, targetPos, targetBox, explosionPos, 10f, raycastFactory);
    }

    public static float explosionDamage(LivingEntity target, Vec3d targetPos, Box targetBox, Vec3d explosionPos, float power, RaycastFactory raycastFactory) {
        double modDistance = distance(targetPos.x, targetPos.y, targetPos.z, explosionPos.x, explosionPos.y, explosionPos.z);
        if (modDistance > power) return 0f;

        double exposure = getExposure(explosionPos, targetBox, raycastFactory);
        double impact = (1 - (modDistance / power)) * exposure;
        float damage = (int) ((impact * impact + impact) / 2 * 7 * 12 + 1);

        return calculateReductions(damage, target, mc.world.getDamageSources().explosion(null));
    }

    public static double distance(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Math.sqrt(squaredDistance(x1, y1, z1, x2, y2, z2));
    }

    private static float getExposure(Vec3d source, Box box, RaycastFactory raycastFactory) {
        double xDiff = box.maxX - box.minX;
        double yDiff = box.maxY - box.minY;
        double zDiff = box.maxZ - box.minZ;

        double xStep = 1 / (xDiff * 2 + 1);
        double yStep = 1 / (yDiff * 2 + 1);
        double zStep = 1 / (zDiff * 2 + 1);

        if (xStep > 0 && yStep > 0 && zStep > 0) {
            int misses = 0;
            int hits = 0;

            double xOffset = (1 - Math.floor(1 / xStep) * xStep) * 0.5;
            double zOffset = (1 - Math.floor(1 / zStep) * zStep) * 0.5;

            xStep = xStep * xDiff;
            yStep = yStep * yDiff;
            zStep = zStep * zDiff;

            double startX = box.minX + xOffset;
            double startY = box.minY;
            double startZ = box.minZ + zOffset;
            double endX = box.maxX + xOffset;
            double endY = box.maxY;
            double endZ = box.maxZ + zOffset;

            for (double x = startX; x <= endX; x += xStep) {
                for (double y = startY; y <= endY; y += yStep) {
                    for (double z = startZ; z <= endZ; z += zStep) {
                        Vec3d position = new Vec3d(x, y, z);

                        if (raycast(new ExposureRaycastContext(position, source), raycastFactory) == null) misses++;

                        hits++;
                    }
                }
            }

            return (float) misses / hits;
        }

        return 0f;
    }



    public static float calculateReductions(float damage, LivingEntity entity, DamageSource damageSource) {
        if (damageSource.isScaledWithDifficulty()) {
            switch (mc.world.getDifficulty()) {
                case EASY     -> damage = Math.min(damage / 2 + 1, damage);
                case HARD     -> damage *= 1.5f;
            }
        }

        // Armor reduction
        damage = DamageUtil.getDamageLeft( damage, getArmor(entity), (float) entity.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS));

        // Resistance reduction
        damage = resistanceReduction(entity, damage);

        // Protection reduction
        damage = protectionReduction(entity, damage, damageSource);

        return Math.max(damage, 0);
    }

    private static float resistanceReduction(LivingEntity player, float damage) {
        StatusEffectInstance resistance = player.getStatusEffect(StatusEffects.RESISTANCE);
        if (resistance != null) {
            int lvl = resistance.getAmplifier() + 1;
            damage *= (1 - (lvl * 0.2f));
        }

        return Math.max(damage, 0);
    }

    private static float protectionReduction(LivingEntity player, float damage, DamageSource source) {
        //fixme float protLevel = EnchantmentHelper.getProtectionAmount(player.getWorld() instanceof ServerWorld serverWorld ? serverWorld : null, player, source);
        return DamageUtil.getInflictedDamage(damage, /*protLevel*/ 0);
    }

    private static BlockHitResult raycast(RaycastContext context, BlockPos ignore, boolean ignoreTerrain) {
        return BlockView.raycast(context.getStart(), context.getEnd(), context, (raycastContext, blockPos) -> {
            BlockState blockState;
            if (blockPos.equals(ignore)) blockState = Blocks.AIR.getDefaultState();
            else {
                blockState = mc.world.getBlockState(blockPos);
                if (blockState.getBlock().getBlastResistance() < 600 && ignoreTerrain)
                    blockState = Blocks.AIR.getDefaultState();
            }

            Vec3d vec3d = raycastContext.getStart();
            Vec3d vec3d2 = raycastContext.getEnd();

            VoxelShape voxelShape = raycastContext.getBlockShape(blockState, mc.world, blockPos);
            BlockHitResult blockHitResult = mc.world.raycastBlock(vec3d, vec3d2, blockPos, voxelShape, blockState);
            VoxelShape voxelShape2 = VoxelShapes.empty();
            BlockHitResult blockHitResult2 = voxelShape2.raycast(vec3d, vec3d2, blockPos);

            double d = blockHitResult == null ? Double.MAX_VALUE : raycastContext.getStart().squaredDistanceTo(blockHitResult.getPos());
            double e = blockHitResult2 == null ? Double.MAX_VALUE : raycastContext.getStart().squaredDistanceTo(blockHitResult2.getPos());

            return d <= e ? blockHitResult : blockHitResult2;
        }, (raycastContext) -> {
            Vec3d vec3d = raycastContext.getStart().subtract(raycastContext.getEnd());
            return BlockHitResult.createMissed(raycastContext.getEnd(), Direction.getFacing(vec3d.x, vec3d.y, vec3d.z), BlockPos.ofFloored(raycastContext.getEnd()));
        });
    }


    private static double normalProtReduction(Entity player, double damage) {
        int protLevel = EnchantmentHelper.getProtectionAmount(player.getArmorItems(), mc.world.getDamageSources().generic());
        if (protLevel > 20) protLevel = 20;

        damage *= 1 - (protLevel / 25.0);
        return damage < 0 ? 0 : damage;
    }

    private static double blastProtReduction(Entity player, double damage, Explosion explosion) {
        int protLevel = EnchantmentHelper.getProtectionAmount(player.getArmorItems(), mc.world.getDamageSources().explosion(explosion));
        if (protLevel > 20) protLevel = 20;

        damage *= (1 - (protLevel / 25.0));
        return damage < 0 ? 0 : damage;
    }


    private static double resistanceReduction(LivingEntity player, double damage) {
        if (player.hasStatusEffect(StatusEffects.RESISTANCE)) {
            int lvl = (player.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1);
            damage *= (1 - (lvl * 0.2));
        }

        return damage < 0 ? 0 : damage;
    }

    private static double getDamageForDifficulty(double damage) {
        return switch (mc.world.getDifficulty()) {
            case EASY -> Math.min(damage / 2 + 1, damage);
            case HARD, PEACEFUL -> damage * 3 / 2;
            default -> damage;
        };
    }

    // Anchor damage

    public static double anchorDamage(LivingEntity player, Box box, BlockPos anchor) {
        return bedDamage(player, box, anchor.toCenterPos(), anchor);
    }
    public static double getDamageTo(final Entity entity,
                                     final Vec3d explosion,
                                     final boolean ignoreTerrain) {
        return getDamageTo(entity, explosion, ignoreTerrain, 12.0f);
    }


    public static double getDamageTo(final Entity entity,
                                     final Vec3d explosion,
                                     final boolean ignoreTerrain,
                                     float power) {
        double d = Math.sqrt(entity.squaredDistanceTo(explosion));
        double ab = getExposure(explosion, entity, ignoreTerrain);
        double w = d / power;
        double ac = (1.0 - w) * ab;
        double dmg = (float) ((int) ((ac * ac + ac) / 2.0 * 7.0 * 12.0 + 1.0));
        dmg = getReduction(entity, mc.world.getDamageSources().explosion(null), dmg);
        return Math.max(0.0, dmg);
    }

    /**
     * @param pos           The actual position of the damage
     * @param entity
     * @param explosion
     * @param ignoreTerrain
     * @return
     */
    public static double getDamageToPos(final Vec3d pos,
                                        final Entity entity,
                                        final Vec3d explosion,
                                        final boolean ignoreTerrain) {
        final Box bb = entity.getBoundingBox();
        double dx = pos.getX() - bb.minX;
        double dy = pos.getY() - bb.minY;
        double dz = pos.getZ() - bb.minZ;
        final Box box = bb.offset(dx, dy, dz);
        //
        double ab = getExposure(explosion, box, ignoreTerrain);
        double w = Math.sqrt(pos.squaredDistanceTo(explosion)) / 12.0;
        double ac = (1.0 - w) * ab;
        double dmg = (float) ((int) ((ac * ac + ac) / 2.0 * 7.0 * 12.0 + 1.0));
        dmg = getReduction(entity, mc.world.getDamageSources().explosion(null), dmg);
        return Math.max(0.0, dmg);
    }

    /**
     * @param entity
     * @param damage
     * @return
     */
    private static double getReduction(Entity entity, DamageSource damageSource, double damage) {
        if (damageSource.isScaledWithDifficulty()) {
            switch (mc.world.getDifficulty()) {
                // case PEACEFUL -> return 0;
                case EASY -> damage = Math.min(damage / 2 + 1, damage);
                case HARD -> damage *= 1.5f;
            }
        }
        if (entity instanceof LivingEntity livingEntity) {
            damage = DamageUtil.getDamageLeft((float) damage, getArmor(livingEntity), (float) getAttributeValue(livingEntity, EntityAttributes.GENERIC_ARMOR_TOUGHNESS));
            damage = getProtectionReduction(entity, damage, damageSource);
        }
        return Math.max(damage, 0);
    }

    private static float getArmor(LivingEntity entity) {
        return (float) Math.floor(getAttributeValue(entity, EntityAttributes.GENERIC_ARMOR));
    }

    private static float getProtectionReduction(Entity player, double damage, DamageSource source) {
        int protLevel = EnchantmentHelper.getProtectionAmount(player.getArmorItems(), source);
        return DamageUtil.getInflictedDamage((float) damage, protLevel);
    }

    public static double getAttributeValue(LivingEntity entity, EntityAttribute attribute) {
        return getAttributeInstance(entity, attribute).getValue();
    }

    public static EntityAttributeInstance getAttributeInstance(LivingEntity entity, EntityAttribute attribute) {
        double baseValue = getDefaultForEntity(entity).getBaseValue(attribute);
        EntityAttributeInstance attributeInstance = new EntityAttributeInstance(attribute, o1 -> {
        });
        attributeInstance.setBaseValue(baseValue);
        for (var equipmentSlot : EquipmentSlot.values()) {
            ItemStack stack = entity.getEquippedStack(equipmentSlot);
            Multimap<EntityAttribute, EntityAttributeModifier> modifiers = stack.getAttributeModifiers(equipmentSlot);
            for (var modifier : modifiers.get(attribute)) attributeInstance.addTemporaryModifier(modifier);
        }
        return attributeInstance;
    }

    private static <T extends LivingEntity> DefaultAttributeContainer getDefaultForEntity(T entity) {
        return DefaultAttributeRegistry.get((EntityType<? extends LivingEntity>) entity.getType());
    }

    /**
     * @param source
     * @param entity
     * @param ignoreTerrain
     * @return
     */
    private static float getExposure(final Vec3d source,
                                     final Entity entity,
                                     final boolean ignoreTerrain) {
        final Box box = entity.getBoundingBox();
        return getExposure(source, box, ignoreTerrain);
    }

    /**
     * @param source
     * @param box
     * @param ignoreTerrain
     * @return
     */
    private static float getExposure(final Vec3d source,
                                     final Box box,
                                     final boolean ignoreTerrain) {
        RaycastFactory raycastFactory = getRaycastFactory(ignoreTerrain);

        double xDiff = box.maxX - box.minX;
        double yDiff = box.maxY - box.minY;
        double zDiff = box.maxZ - box.minZ;

        double xStep = 1 / (xDiff * 2 + 1);
        double yStep = 1 / (yDiff * 2 + 1);
        double zStep = 1 / (zDiff * 2 + 1);

        if (xStep > 0 && yStep > 0 && zStep > 0) {
            int misses = 0;
            int hits = 0;

            double xOffset = (1 - Math.floor(1 / xStep) * xStep) * 0.5;
            double zOffset = (1 - Math.floor(1 / zStep) * zStep) * 0.5;

            xStep = xStep * xDiff;
            yStep = yStep * yDiff;
            zStep = zStep * zDiff;

            double startX = box.minX + xOffset;
            double startY = box.minY;
            double startZ = box.minZ + zOffset;
            double endX = box.maxX + xOffset;
            double endY = box.maxY;
            double endZ = box.maxZ + zOffset;

            for (double x = startX; x <= endX; x += xStep) {
                for (double y = startY; y <= endY; y += yStep) {
                    for (double z = startZ; z <= endZ; z += zStep) {
                        Vec3d position = new Vec3d(x, y, z);

                        if (raycast(new ExposureRaycastContext(position, source), raycastFactory) == null) misses++;

                        hits++;
                    }
                }
            }

            return (float) misses / hits;
        }

        return 0f;
    }

    private static RaycastFactory getRaycastFactory(boolean ignoreTerrain) {
        if (ignoreTerrain) {
            return (context, blockPos) -> {
                BlockState blockState = mc.world.getBlockState(blockPos);
                if (blockState.getBlock().getBlastResistance() < 600) return null;

                return blockState.getCollisionShape(mc.world, blockPos).raycast(context.start(), context.end(), blockPos);
            };
        } else {
            return (context, blockPos) -> {
                BlockState blockState = mc.world.getBlockState(blockPos);
                return blockState.getCollisionShape(mc.world, blockPos).raycast(context.start(), context.end(), blockPos);
            };
        }
    }

    /* Raycasts */

    private static BlockHitResult raycast(ExposureRaycastContext context, RaycastFactory raycastFactory) {
        return BlockView.raycast(context.start, context.end, context, raycastFactory, ctx -> null);
    }

    public record ExposureRaycastContext(Vec3d start, Vec3d end) {
    }

    @FunctionalInterface
    public interface RaycastFactory extends BiFunction<ExposureRaycastContext, BlockPos, BlockHitResult> {
    }
}
