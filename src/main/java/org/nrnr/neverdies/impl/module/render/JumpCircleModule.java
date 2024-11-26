package org.nrnr.neverdies.impl.module.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.config.setting.EnumConfig;
import org.nrnr.neverdies.api.config.setting.NumberConfig;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.init.Modules;
import org.nrnr.neverdies.util.chat.ChatUtil;
import org.nrnr.neverdies.util.math.timer.Timer;


import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.nrnr.neverdies.impl.module.render.JumpCircleModule.Mode.Custom;
import static org.nrnr.neverdies.impl.module.render.JumpCircleModule.Mode.Default;


public class JumpCircleModule extends ToggleModule {
    public JumpCircleModule() {
        super("JumpCircle", "hi", ModuleCategory.RENDER);
    }

    public Config<Mode> mode = new EnumConfig<>("Mode", "Smooth", Default, Mode.values());
    public Config<Boolean> easeOut = new BooleanConfig("EaseOut", "Smooth Renders", true);
    public Config<Float> rotateSpeed = new NumberConfig<>("RotateSpeed","Rotate Speed", 2f, 0.5f, 5f);
    public Config<Float> circleScale = new NumberConfig<>("CircleScale", "Scale", 1f, 0.5f, 5f);
    public Config<Boolean> onlySelf = new BooleanConfig("OnlySelf", "Self Renders", false);
    private final List<Circle> circles = new ArrayList<>();
    private final List<PlayerEntity> cache = new CopyOnWriteArrayList<>();
    private Identifier custom;

    @Override
    public void onEnable() {
        try {
            System.out.println("hi");
            custom = new Identifier("neverdies", "textures/circle.png");


        } catch (Exception ignored) {
        }
    }

    @EventListener
    public void onTick() {
        if (mode.equals(Custom) && custom == null) {
            try {
                custom = new Identifier("neverdies", "textures/circle.png");
            } catch (Exception e) {
                ChatUtil.clientSendMessage(".minecraft -> ThunderHackRecode -> misc -> images -> circle.png");
            }
        }

        for (PlayerEntity pl : mc.world.getPlayers())
            if (!cache.contains(pl) && pl.isOnGround() && (mc.player == pl || !onlySelf.getValue()))
                cache.add(pl);

        cache.forEach(pl -> {
            if (pl != null && !pl.isOnGround()) {
                circles.add(new Circle(new Vec3d(pl.getX(), (int) Math.floor(pl.getY()) + 0.001f, pl.getZ()), new Timer() {
                    @Override
                    public boolean passed(Number time) {
                        return false;
                    }

                    @Override
                    public void reset() {

                    }

                    @Override
                    public long getElapsedTime() {
                        return 0;
                    }

                    @Override
                    public void setElapsedTime(Number time) {

                    }
                }));
                cache.remove(pl);
            }
        });

        circles.removeIf(c -> c.timer.passed(easeOut.getValue() ? 5000 : 6000));
    }

    @EventListener
    public void onRenderWorld(MatrixStack stack) {
        Collections.reverse(circles);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);

        Identifier default_circle = new Identifier("neverdies", "textures/circle.png");
        Identifier bubble = new Identifier("neverdies", "textures/bubble.png");

        switch (mode.getValue()) {
            case Portal -> RenderSystem.setShaderTexture(0, bubble);
            case Default -> RenderSystem.setShaderTexture(0, default_circle);
            case Custom ->
                    RenderSystem.setShaderTexture(0, Objects.requireNonNullElse(custom, default_circle));
        }

        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();

        for (Circle c : circles) {
            float colorAnim = (float) (c.timer.getElapsedTime()) / 6000f;
            float sizeAnim = circleScale.getValue() - (float) Math.pow(1 - ((c.timer.getElapsedTime() * (easeOut.getValue() ? 2f : 1f)) / 5000f), 4);

            stack.push();
            stack.translate(c.pos().x - mc.getEntityRenderDispatcher().camera.getPos().getX(), c.pos().y - mc.getEntityRenderDispatcher().camera.getPos().getY(), c.pos().z - mc.getEntityRenderDispatcher().camera.getPos().getZ());
            stack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
            stack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(sizeAnim * rotateSpeed.getValue() * 1000f));
            float scale = sizeAnim * 2f;
            Matrix4f matrix = stack.peek().getPositionMatrix();

            buffer.vertex(matrix, -sizeAnim, -sizeAnim + scale, 0).texture(0, 1).color(applyOpacity(Modules.COLORS.getColor(270), 1f - colorAnim).getRGB());
            buffer.vertex(matrix, -sizeAnim + scale, -sizeAnim + scale, 0).texture(1, 1).color(applyOpacity(Modules.COLORS.getColor(0), 1f - colorAnim).getRGB());
            buffer.vertex(matrix, -sizeAnim + scale, -sizeAnim, 0).texture(1, 0).color(applyOpacity(Modules.COLORS.getColor(180), 1f - colorAnim).getRGB());
            buffer.vertex(matrix, -sizeAnim, -sizeAnim, 0).texture(0, 0).color(applyOpacity(Modules.COLORS.getColor(90), 1f - colorAnim).getRGB());

            stack.pop();
        }

        endBuilding(buffer);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.enableDepthTest();
        Collections.reverse(circles);
    }

    public static Color applyOpacity(Color color, float opacity) {
        opacity = Math.min(1, Math.max(0, opacity));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (color.getAlpha() * opacity));
    }

    public static void endBuilding(BufferBuilder bb) {
        BufferBuilder.BuiltBuffer builtBuffer = bb.endNullable();
        if (builtBuffer != null)
            BufferRenderer.drawWithGlobalProgram(builtBuffer);
    }

    public enum Mode {
        Default, Portal, Custom
    }

    public record Circle(Vec3d pos, Timer timer) {
    }
}