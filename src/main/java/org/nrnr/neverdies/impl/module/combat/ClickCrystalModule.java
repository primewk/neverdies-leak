package org.nrnr.neverdies.impl.module.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.EndCrystalItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.config.setting.NumberConfig;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.RotationModule;
import org.nrnr.neverdies.impl.event.network.PacketEvent;
import org.nrnr.neverdies.impl.event.network.PlayerTickEvent;
import org.nrnr.neverdies.impl.event.world.AddEntityEvent;
import org.nrnr.neverdies.impl.event.world.RemoveEntityEvent;
import org.nrnr.neverdies.init.Managers;
import org.nrnr.neverdies.util.player.RotationUtil;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ClickCrystalModule extends RotationModule {

    Config<Float> breakDelayConfig = new NumberConfig<>("SpawnDelay", "Speed to break crystals after spawning", 0.0f, 0.0f, 20.0f);
    Config<Float> randomDelayConfig = new NumberConfig<>("RandomDelay", "Randomized break delay", 0.0f, 0.0f, 5.0f);
    Config<Boolean> rotateConfig = new BooleanConfig("Rotate", "Rotate before breaking", false);
    Config<Boolean> randomRotateConfig = new BooleanConfig("Rotate-Random", "Slightly randomizes rotations", false, () -> rotateConfig.getValue());
    private final Set<BlockPos> placedCrystals = new HashSet<>();
    private final Map<EndCrystalEntity, Long> spawnedCrystals = new LinkedHashMap<>();
    private float randomDelay = -1;

    public ClickCrystalModule() {
        super("ClickCrystal", "Automatically breaks placed crystals", ModuleCategory.LEGIT);
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event) {
        if (spawnedCrystals.isEmpty()) {
            return;
        }
        Map.Entry<EndCrystalEntity, Long> e = spawnedCrystals.entrySet().iterator().next();
        EndCrystalEntity crystalEntity = e.getKey();
        Long time = e.getValue();
        if (randomDelay == -1) {
            randomDelay = randomDelayConfig.getValue() == 0.0f ? 0.0f : RANDOM.nextFloat(randomDelayConfig.getValue() * 25.0f);
        }
        float breakDelay = breakDelayConfig.getValue() * 50.0f + randomDelay;
        if (mc.player.getEyePos().squaredDistanceTo(crystalEntity.getPos()) <= 12.25 && System.currentTimeMillis() - time >= breakDelay) {
            if (rotateConfig.getValue()) {
                Vec3d rotatePos = crystalEntity.getPos();
                if (randomRotateConfig.getValue()) {
                    Box bb = crystalEntity.getBoundingBox();
                    rotatePos = new Vec3d(RANDOM.nextDouble(bb.minX, bb.maxX), RANDOM.nextDouble(bb.minY, bb.maxY), RANDOM.nextDouble(bb.minZ, bb.maxZ));
                }
                float[] rotations = RotationUtil.getRotationsTo(mc.player.getEyePos(), rotatePos);
                setRotation(rotations[0], rotations[1]);
            }
            Managers.NETWORK.sendPacket(PlayerInteractEntityC2SPacket.attack(crystalEntity, mc.player.isSneaking()));
            mc.player.swingHand(Hand.MAIN_HAND);
            randomDelay = -1;
        }
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event) {
        if (event.getPacket() instanceof PlayerInteractBlockC2SPacket packet && !event.isClientPacket() && mc.player.getStackInHand(packet.getHand()).getItem() instanceof EndCrystalItem) {
            placedCrystals.add(packet.getBlockHitResult().getBlockPos());
        }
    }

    @EventListener
    public void onAddEntity(AddEntityEvent event) {
        if (event.getEntity() instanceof EndCrystalEntity crystalEntity) {
            BlockPos base = crystalEntity.getBlockPos().down();
            if (placedCrystals.contains(base)) {
                spawnedCrystals.put(crystalEntity, System.currentTimeMillis());
                placedCrystals.remove(base);
            }
        }
    }

    @EventListener
    public void onRemoveEntity(RemoveEntityEvent event) {
        if (event.getEntity() instanceof EndCrystalEntity crystalEntity) {
            spawnedCrystals.remove(crystalEntity);
        }
    }
}
