package org.nrnr.neverdies.impl.module.movement;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.*;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.config.setting.EnumConfig;
import org.nrnr.neverdies.api.config.setting.NumberConfig;
import org.nrnr.neverdies.api.event.EventStage;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.api.render.RenderManager;
import org.nrnr.neverdies.impl.event.TickEvent;
import org.nrnr.neverdies.impl.event.gui.hud.RenderOverlayEvent;
import org.nrnr.neverdies.impl.event.network.DisconnectEvent;
import org.nrnr.neverdies.impl.event.network.PacketEvent;
import org.nrnr.neverdies.impl.event.render.RenderWorldEvent;
import org.nrnr.neverdies.init.Managers;
import org.nrnr.neverdies.init.Modules;
import org.nrnr.neverdies.util.world.FakePlayerEntity;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author chronos
 * @since 1.0
 */
public class BlinkModule extends ToggleModule {

    //
    Config<LagMode> modeConfig = new EnumConfig<>("Mode", "The mode for caching packets", LagMode.BLINK, LagMode.values());
    Config<Boolean> pulseConfig = new BooleanConfig("Pulse", "Releases packets at intervals", false);
    Config<Float> factorConfig = new NumberConfig<>("Factor", "The factor for packet intervals", 0.0f, 1.0f, 10.0f, () -> pulseConfig.getValue());
    Config<Boolean> renderConfig = new BooleanConfig("Render", "Renders the serverside player position", true);
    //
    private FakePlayerEntity serverModel;
    private boolean shouldRenderOverlay = false;
    //
    private boolean blinking;
    private final Queue<Packet<?>> packets = new LinkedBlockingQueue<>();

    /**
     *
     */
    public BlinkModule() {
        super("Blink", "Also known as FakeLag. You know the drill!", ModuleCategory.MOVEMENT);
    }

    @Override
    public void onEnable() {
        if (renderConfig.getValue()) {
            serverModel = new FakePlayerEntity(mc.player, mc.getGameProfile());
            serverModel.despawnPlayer();
            serverModel.spawnPlayer();
        }
    }

    @Override
    public void onDisable() {
        if (mc.player == null) {
            return;
        }
        if (!packets.isEmpty()) {
            for (Packet<?> p : packets) {
                Managers.NETWORK.sendPacket(p);
            }
            packets.clear();
        }
        if (serverModel != null) {
            serverModel.despawnPlayer();
        }
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() == EventStage.PRE && pulseConfig.getValue() && packets.size() > factorConfig.getValue() * 10.0f) {
            blinking = true;
            if (!packets.isEmpty()) {
                for (Packet<?> p : packets) {
                    Managers.NETWORK.sendPacket(p);
                }
            }
            packets.clear();
            if (serverModel != null) {
                serverModel.copyPositionAndRotation(mc.player);
                serverModel.setHeadYaw(mc.player.headYaw);
            }
            blinking = false;
        }
    }

    @EventListener
    public void onDisconnectEvent(DisconnectEvent event) {
        // packets.clear();
        disable();
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event) {
        if (mc.player == null || mc.player.isRiding() || blinking) {
            return;
        }
        if (event.getPacket() instanceof PlayerActionC2SPacket || event.getPacket() instanceof PlayerMoveC2SPacket
                || event.getPacket() instanceof ClientCommandC2SPacket || event.getPacket() instanceof HandSwingC2SPacket
                || event.getPacket() instanceof PlayerInteractEntityC2SPacket || event.getPacket() instanceof PlayerInteractBlockC2SPacket
                || event.getPacket() instanceof PlayerInteractItemC2SPacket) {
            event.cancel();
            packets.add(event.getPacket());
        }
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event) {
        shouldRenderOverlay = true;
    }


    @EventListener
    public void onRenderOverlayPost(RenderOverlayEvent.Post event) {
        if (renderConfig.getValue()){
            MinecraftClient client = MinecraftClient.getInstance();
            int screenWidth = client.getWindow().getScaledWidth();
            int screenHeight = client.getWindow().getScaledHeight();

            MatrixStack matrixStack = new MatrixStack();
            matrixStack.push();
            RenderManager.renderText(event.getContext(),"You are Currently Blinked", ((screenWidth / 2) - (client.textRenderer.getWidth("You are Currently Blinked") / 2)),
                    (screenHeight / 2) - (client.textRenderer.fontHeight / 2) + 25, Modules.COLORS.getRGB());
            RenderManager.renderLine(event.getContext().getMatrices(), mc.player.getX(), mc.player.getY(), mc.player.getZ(), serverModel.getX(), serverModel.getY(), serverModel.getZ(), 15f, Modules.COLORS.getRGB());

            matrixStack.pop();
        }


    }

        public enum LagMode {
        BLINK
    }
}
