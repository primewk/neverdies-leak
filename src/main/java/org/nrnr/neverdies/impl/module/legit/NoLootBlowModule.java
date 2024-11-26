package org.nrnr.neverdies.impl.module.legit;


import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.Box;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.config.setting.NumberConfig;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.RotationModule;
import org.nrnr.neverdies.util.chat.ChatUtil;
import net.minecraft.util.math.Vec3d;

public class NoLootBlowModule extends RotationModule {


    Config<Boolean> preserveItems = new BooleanConfig("NoLootPop", "enable this", true);
    Config<Integer> lootProtectRadiusX = new NumberConfig<>("ProtectX", "loot protext x axis", 0, 8, 16);
    Config<Integer> lootProtectRadiusY = new NumberConfig<>("ProtectY", "loot protext y axis", 0, 8, 16);
    Config<Integer> lootProtectRadiusZ = new NumberConfig<>("ProtectZ", "loot protext z axis", 0, 8, 16);

    public NoLootBlowModule() {
        super("NoLootBlow", "Prevents the player from blowing up valuable loot with end crystals", ModuleCategory.LEGIT);
    }

    public void onTick() {
        Entity target = raycastEndCrystal(5);
        if (target == null)
            return;

        if (preserveItems.getValue() && ItemNearby(target, 6)) {
            // TODO: Prevent the attack action from happening
        }
    }



    private Entity raycastEndCrystal(double range) {
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        Vec3d viewVector = mc.player.getRotationVecClient();
        Vec3d extendedPoint = cameraPos.add(viewVector.x * range, viewVector.y * range, viewVector.z * range);

        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof EndCrystalEntity) {
                if (entity.getBoundingBox().expand(0.3).intersects(cameraPos, extendedPoint)) {
                    return entity;
                }
            }
        }

        return null;
    }

    private boolean ItemNearby(Entity entity, double range) {
        Box boundingBox = new Box(
                entity.getPos().x - lootProtectRadiusX.getValue(),
                entity.getPos().y - lootProtectRadiusY.getValue(),
                entity.getPos().z - lootProtectRadiusZ.getValue(),
                entity.getPos().x + lootProtectRadiusX.getValue(),
                entity.getPos().y + lootProtectRadiusY.getValue(),
                entity.getPos().z + lootProtectRadiusZ.getValue()
        );

        for (Entity nearbyEntity : mc.world.getOtherEntities(null, boundingBox)) {
            if (nearbyEntity instanceof ItemEntity) {
                if (isPreciousItem(((ItemEntity) nearbyEntity).getStack().getItem())) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isPreciousItem(Item item) {
        return item == Items.DIAMOND ||
                item == Items.DIAMOND_BLOCK ||
                item == Items.DIAMOND_SWORD ||
                item == Items.DIAMOND_PICKAXE ||
                item == Items.DIAMOND_AXE ||
                item == Items.DIAMOND_SHOVEL ||
                item == Items.DIAMOND_HOE ||
                item == Items.DIAMOND_HELMET ||
                item == Items.DIAMOND_CHESTPLATE ||
                item == Items.DIAMOND_LEGGINGS ||
                item == Items.DIAMOND_BOOTS ||
                item == Items.NETHERITE_INGOT ||
                item == Items.NETHERITE_BLOCK ||
                item == Items.NETHERITE_SWORD ||
                item == Items.NETHERITE_PICKAXE ||
                item == Items.NETHERITE_AXE ||
                item == Items.NETHERITE_SHOVEL ||
                item == Items.NETHERITE_HOE ||
                item == Items.NETHERITE_HELMET ||
                item == Items.NETHERITE_CHESTPLATE ||
                item == Items.NETHERITE_LEGGINGS ||
                item == Items.NETHERITE_BOOTS;
    }
}
