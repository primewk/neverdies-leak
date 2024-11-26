package org.nrnr.neverdies.impl.module.combat;

import net.minecraft.entity.LivingEntity;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import net.minecraft.network.packet.s2c.common.KeepAliveS2CPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.NumberConfig;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.api.render.RenderManager;
import org.nrnr.neverdies.impl.event.network.PacketEvent;
import org.nrnr.neverdies.impl.event.network.PlayerTickEvent;
import org.nrnr.neverdies.impl.event.render.RenderWorldEvent;
import org.nrnr.neverdies.init.Managers;
import org.nrnr.neverdies.init.Modules;
import org.nrnr.neverdies.util.math.timer.TickTimer;
import org.nrnr.neverdies.util.math.timer.Timer;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author xgraza
 * @since 1.0
 */
public final class BackTrackModule extends ToggleModule {
    Config<Integer> delayConfig = new NumberConfig<>("Delay", "The delay before throttling packets again", 0, 100, 1000);

    private final Queue<Packet<?>> packetQueue = new ConcurrentLinkedQueue<>();
    private boolean blockingPackets;

    private final Timer timer = new TickTimer();
    private LivingEntity attackingEntity;
    private PacketListener packetListener;
    private Vec3d lastServerPos, serverPos;
    private Box hitBox;

    public BackTrackModule() {
        super("Backtrack", "funny", ModuleCategory.COMBAT);
    }

    @Override
    protected void onDisable() {
        super.onDisable();

        if (packetListener != null) {
            emptyPackets();
        }
        packetQueue.clear();
        packetListener = null;

        serverPos = null;
        attackingEntity = null;
        hitBox = null;
    }

    @EventListener
    public void onPacketInbound(final PacketEvent.Inbound event) {
        if (mc.world != null && mc.player != null) {
            packetListener = event.getPacketListener();
        } else {
            packetQueue.clear();
            blockingPackets = false;
        }

        if (event.getPacket() instanceof EntityPositionS2CPacket packet) {
            if (attackingEntity != null && attackingEntity.getId() == packet.getId()) {
                lastServerPos = serverPos;
                serverPos = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
            }
        }

        if (!blockingPackets || packetListener == null) {
            return;
        }

        if (shouldCancelPacket(event.getPacket())) {
            event.setCanceled(true);
            packetQueue.add(event.getPacket());
        }
    }

    @EventListener
    public void onPlayerTick(final PlayerTickEvent event) {
        final AuraModule auraModule = Modules.AURA;
        if (!auraModule.isEnabled() || !(auraModule.getEntityTarget() instanceof LivingEntity auraTarget)) {
            attackingEntity = null;
            if (blockingPackets) {
                emptyPackets();
            }
            blockingPackets = false;
            serverPos = null;
            lastServerPos = null;
            hitBox = null;
            return;
        }

        attackingEntity = auraTarget;

        if (!timer.passed(delayConfig.getValue() / 50)) {
            return;
        }

        if (hitBox != null) {
            final Vec3d eyes = Managers.POSITION.getEyePos();
            final double dist = eyes.distanceTo(attackingEntity.getPos());
            if (!auraModule.isInAttackRange(dist, eyes, attackingEntity.getPos()) && dist <= auraModule.searchRangeConfig.getValue()) {
                blockingPackets = true;
            } else {
                blockingPackets = false;
                timer.reset();
                emptyPackets();
            }
        }
    }

    @EventListener
    public void onRenderWorld(final RenderWorldEvent event) {
        if (mc.player == null || mc.world == null) {
            return;
        }

        hitBox = getBackTrackedBox(event.getTickDelta());

        if (hitBox != null) {
            RenderManager.renderBox(event.getMatrices(), hitBox, Modules.COLORS.getRGB(120));
            RenderManager.renderBoundingBox(event.getMatrices(), hitBox, 1.5f, Modules.COLORS.getRGB());
        }
    }

    private Box getBackTrackedBox(final float tickDelta) {
        if (serverPos == null || attackingEntity == null) {
            return null;
        }

//        final double intX = MathHelper.lerp(tickDelta, lastServerPos.x, serverPos.x);
//        final double intY = MathHelper.lerp(tickDelta, lastServerPos.y, serverPos.y);
//        final double intZ = MathHelper.lerp(tickDelta, lastServerPos.z, serverPos.z);

        return attackingEntity.getDimensions(attackingEntity.getPose()).getBoxAt(serverPos.x, serverPos.y, serverPos.z);
    }

    private boolean shouldCancelPacket(final Packet<?> packet) {
        if (Modules.VELOCITY.isEnabled()) {
            return !(packet instanceof EntityVelocityUpdateS2CPacket || packet instanceof ExplosionS2CPacket);
        }

        return !(packet instanceof HealthUpdateS2CPacket
                || packet instanceof PlaySoundS2CPacket
                || packet instanceof StopSoundS2CPacket
                || packet instanceof EntitySpawnS2CPacket
                || packet instanceof EntityStatusS2CPacket
                || packet instanceof TeamS2CPacket
                || packet instanceof CommonPingS2CPacket
                || packet instanceof KeepAliveS2CPacket);
    }

    private void emptyPackets() {
        if (packetQueue.isEmpty() || packetListener == null) {
            return;
        }

        while (!packetQueue.isEmpty()) {
            final Packet<?> polled = packetQueue.poll();
            if (polled == null) {
                break;
            }

            packetListener.accepts(polled);
        }
    }
}
