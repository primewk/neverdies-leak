package org.nrnr.neverdies.impl.module.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EndCrystalEntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

import org.joml.Quaternionf;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.config.setting.ColorConfig;
import org.nrnr.neverdies.api.config.setting.EnumConfig;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.impl.event.render.RenderWorldEvent;
import org.nrnr.neverdies.impl.event.render.item.RenderFirstPersonEvent;
import org.nrnr.neverdies.init.Managers;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;


public class ChamsRewriteModule extends ToggleModule {
    public Config<Boolean> handItems = new BooleanConfig("HandItems", "Render chams on first-person hands", true);
    public Config<Color> handItemsColor = new ColorConfig("HandItemsColor", "The color of the chams", new Color(255, 0, 0, 60), handItems::getValue);

    public Config<Boolean> crystals = new BooleanConfig("Crystals", "Render chams on first-person hands", true);
    public Config<Color> crystalColor = new ColorConfig("CrystalColor", "The color of the chams", new Color(255, 0, 0, 60), crystals::getValue);
    public Config<Boolean> staticCrystal = new BooleanConfig("StaticCrystal", "Render chams on first-person hands", true, crystals::getValue);
    public Config<CMode> crystalMode = new EnumConfig<>("CrystalMode", "The rendering mode for the chams", CMode.Default, CMode.values(), crystals::getValue);

    public Config<Boolean> players = new BooleanConfig("Players", "Render chams on first-person hands", true);
    public Config<Color> playerColor = new ColorConfig("PlayerColor", "The color of the chams", new Color(255, 0, 0, 60), players::getValue);
    public Config<Color> friendColor = new ColorConfig("FriendColor", "The color of the chams", new Color(255, 0, 0, 60), players::getValue);
    public Config<Boolean> playerTexture = new BooleanConfig("PlayerTexture", "Render chams on first-person hands", true, players::getValue);
    public Config<Boolean> simple = new BooleanConfig("Simple", "Render chams on first-person hands", true, players::getValue);

    public Config<Boolean> alternativeBlending = new BooleanConfig("Shine", "Glow Chams", false);

    private static final float SINE_45_DEGREES = (float) Math.sin(0.7853981633974483);
    private final Identifier crystalTexture = new Identifier("textures/entity/end_crystal/end_crystal.png");
    private final Identifier crystalTexture2 = new Identifier("neverdies", "textures/end_crystal2.png");

    public ChamsRewriteModule() {
        super("ChamsRewrite", "Renders entities through walls", ModuleCategory.RENDER);
    }

    public void renderCrystal(EndCrystalEntity endCrystalEntity, float f, float g, MatrixStack matrixStack, int i, ModelPart core, ModelPart frame) {
        RenderSystem.enableBlend();
        if(alternativeBlending.getValue()) RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        else RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        if (crystalMode.getValue() != CMode.Default) {
            if (crystalMode.getValue() == CMode.Texture) {
                RenderSystem.setShaderTexture(0, crystalTexture);
            } else {
                RenderSystem.setShaderTexture(0, crystalTexture2);
            }
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        } else {
            RenderSystem.setShader(GameRenderer::getPositionProgram);
            buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        }

        matrixStack.push();
        float h = staticCrystal.getValue() ? -1.4f : EndCrystalEntityRenderer.getYOffset(endCrystalEntity, g);
        float j = ((float) endCrystalEntity.endCrystalAge + g) * 3.0f;
        matrixStack.push();
        RenderSystem.setShaderColor(crystalColor.getValue().getRed() / 255f, crystalColor.getValue().getGreen() / 255f, crystalColor.getValue().getBlue() / 255f, crystalColor.getValue().getAlpha() / 255f);
        matrixStack.scale(2.0f, 2.0f, 2.0f);
        matrixStack.translate(0.0f, -0.5f, 0.0f);
        int k = OverlayTexture.DEFAULT_UV;
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
        matrixStack.translate(0.0f, 1.5f + h / 2.0f, 0.0f);
        matrixStack.multiply(new Quaternionf().setAngleAxis(1.0471976f, SINE_45_DEGREES, 0.0f, SINE_45_DEGREES));
        frame.render(matrixStack, buffer, i, k);
        matrixStack.scale(0.875f, 0.875f, 0.875f);
        matrixStack.multiply(new Quaternionf().setAngleAxis(1.0471976f, SINE_45_DEGREES, 0.0f, SINE_45_DEGREES));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
        frame.render(matrixStack, buffer, i, k);
        matrixStack.scale(0.875f, 0.875f, 0.875f);
        matrixStack.multiply(new Quaternionf().setAngleAxis(1.0471976f, SINE_45_DEGREES, 0.0f, SINE_45_DEGREES));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
        core.render(matrixStack, buffer, i, k);
        matrixStack.pop();
        matrixStack.pop();
        tessellator.draw();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
    }

    public void renderPlayer(PlayerEntity pe, float f, float g, MatrixStack matrixStack, int i, EntityModel model, CallbackInfo ci, Runnable post) {
        RenderSystem.enableBlend();
        if(alternativeBlending.getValue()) RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        else RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        if (!simple.getValue()) {
            RenderSystem.setShaderTexture(0, ((AbstractClientPlayerEntity) pe).getSkinTextures().texture());
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        } else {
            RenderSystem.setShader(GameRenderer::getPositionProgram);
            buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        }

        float n;
        Direction direction;
        Entity entity;
        matrixStack.push();
        if (Managers.SOCIAL.isFriend(pe.getName())) RenderSystem.setShaderColor(friendColor.getValue().getRed() / 255f, friendColor.getValue().getGreen() / 255f, friendColor.getValue().getBlue() / 255f, friendColor.getValue().getAlpha() / 255f);
        else RenderSystem.setShaderColor(playerColor.getValue().getRed() / 255f, playerColor.getValue().getGreen() / 255f, playerColor.getValue().getBlue() / 255f, playerColor.getValue().getAlpha() / 255f);
        model.handSwingProgress = pe.getHandSwingProgress(g);
        model.riding = pe.hasVehicle();
        model.child = false;
        float h = MathHelper.lerpAngleDegrees(g, pe.prevBodyYaw, pe.bodyYaw);
        float j = MathHelper.lerpAngleDegrees(g, pe.prevHeadYaw, pe.headYaw);
        float k = j - h;
        if (pe.hasVehicle() && (entity = pe.getVehicle()) instanceof LivingEntity) {
            LivingEntity livingEntity2 = (LivingEntity) entity;
            h = MathHelper.lerpAngleDegrees(g, livingEntity2.prevBodyYaw, livingEntity2.bodyYaw);
            k = j - h;
            float l = MathHelper.wrapDegrees(k);
            if (l < -85.0f) {
                l = -85.0f;
            }
            if (l >= 85.0f) {
                l = 85.0f;
            }
            h = j - l;
            if (l * l > 2500.0f) {
                h += l * 0.2f;
            }
            k = j - h;
        }
        float m = MathHelper.lerp(g, pe.prevPitch, pe.getPitch());
        if (LivingEntityRenderer.shouldFlipUpsideDown(pe)) {
            m *= -1.0f;
            k *= -1.0f;
        }
        if (pe.isInPose(EntityPose.SLEEPING) && (direction = pe.getSleepingDirection()) != null) {
            n = pe.getEyeHeight(EntityPose.STANDING) - 0.1f;
            matrixStack.translate((float) (-direction.getOffsetX()) * n, 0.0f, (float) (-direction.getOffsetZ()) * n);
        }
        float l = pe.age + g;
        this.setupTransforms(pe, matrixStack, l, h, g);
        matrixStack.scale(-1.0f, -1.0f, 1.0f);
        matrixStack.scale(0.9375f, 0.9375f, 0.9375f);
        matrixStack.translate(0.0f, -1.501f, 0.0f);
        n = 0.0f;
        float o = 0.0f;
        if (!pe.hasVehicle() && pe.isAlive()) {
            n = pe.limbAnimator.getSpeed(g);
            o = pe.limbAnimator.getPos(g);
            if (pe.isBaby())
                o *= 3.0f;

            if (n > 1.0f)
                n = 1.0f;
        }
        model.animateModel(pe, o, n, g);
        model.setAngles(pe, o, n, l, k, m);
        boolean bl = !pe.isInvisible();
        boolean bl2 = !bl && !pe.isInvisibleTo(mc.player);
        int p = LivingEntityRenderer.getOverlay(pe, 0);
        model.render(matrixStack, buffer, i, p, 1.0f, 1.0f, 1.0f, bl2 ? 0.15f : 1.0f);
        tessellator.draw();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
        RenderSystem.disableCull();
        matrixStack.pop();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        if (!playerTexture.getValue()) {
            ci.cancel();
            post.run();
        }
    }

    public void setupTransforms(PlayerEntity entity, MatrixStack matrices, float animationProgress, float bodyYaw, float tickDelta) {
        if (entity.isFrozen()) {
            bodyYaw += (float) (Math.cos((double) entity.age * 3.25) * Math.PI * (double) 0.4f);
        }
        if (!entity.isInPose(EntityPose.SLEEPING)) {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - bodyYaw));
        }
        if (entity.deathTime > 0) {
            float f = ((float) entity.deathTime + tickDelta - 1.0f) / 20.0f * 1.6f;
            if ((f = MathHelper.sqrt(f)) > 1.0f) {
                f = 1.0f;
            }
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(f * 90));
        } else if (entity.isUsingRiptide()) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0f - entity.getPitch()));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(((float) entity.age + tickDelta) * -75.0f));
        } else if (entity.isInPose(EntityPose.SLEEPING)) {
            Direction direction = entity.getSleepingDirection();
            float g = direction != null ? getYaw(direction) : bodyYaw;
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(g));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(270.0f));
        } else if (LivingEntityRenderer.shouldFlipUpsideDown(entity)) {
            matrices.translate(0.0f, entity.getHeight() + 0.1f, 0.0f);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0f));
        }
    }

    private static float getYaw(Direction direction) {
        switch (direction) {
            case SOUTH: {
                return 90.0f;
            }
            case WEST: {
                return 0.0f;
            }
            case NORTH: {
                return 270.0f;
            }
            case EAST: {
                return 180.0f;
            }
        }
        return 0.0f;
    }


    @EventListener
    public void onRenderFirstPerson(RenderFirstPersonEvent event) {
       // if (handItems.getValue())
       //     RenderSystem.setShaderColor(handItemsColor.getValue().getRed() / 255f, handItemsColor.getValue().getGreen() / 255f, handItemsColor.getValue().getBlue() / 255f, handItemsColor.getValue().getAlpha() / 255f);
    }


    public enum CMode {
        Default, Wireframe, Texture
    }
}
