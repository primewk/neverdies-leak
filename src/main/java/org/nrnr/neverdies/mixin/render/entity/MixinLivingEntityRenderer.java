package org.nrnr.neverdies.mixin.render.entity;


import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.nrnr.neverdies.Neverdies;
import org.nrnr.neverdies.impl.event.render.entity.RenderEntityInvisibleEvent;
import org.nrnr.neverdies.init.Modules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer<T extends LivingEntity, M extends EntityModel<T>> {
    //
    @Shadow
    protected M model;
    //
    @Shadow
    @Final
    protected List<FeatureRenderer<T, M>> features;

    @Shadow
    protected abstract RenderLayer getRenderLayer(T entity, boolean showBody, boolean translucent, boolean showOutline);

    /**
     * @param livingEntity
     * @param f
     * @param g
     * @param matrixStack
     * @param vertexConsumerProvider
     * @param i
     * @param ci
     */


    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void onRenderPre(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (livingEntity instanceof PlayerEntity pe && Modules.CHAMS_REWRITE.isEnabled() && Modules.CHAMS_REWRITE.players.getValue()) {
            Modules.CHAMS_REWRITE.renderPlayer(pe, f, g, matrixStack, i, model, ci, () -> {});
            if (!pe.isSpectator()) {
                float n;
                Direction direction;
                Entity entity;
                matrixStack.push();
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
                Modules.CHAMS_REWRITE.setupTransforms(pe, matrixStack, l, h, g);
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

                for (FeatureRenderer<T, M> featureRenderer : features) {
                    featureRenderer.render(matrixStack, vertexConsumerProvider, i, livingEntity, o, n, g, l, k, m);
                }
                matrixStack.pop();
            }
        }
    }

    @Redirect(method = "render*", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isInvisibleTo(Lnet/minecraft/entity/player/PlayerEntity;)Z"))
    private boolean redirectRender$isInvisibleTo(LivingEntity entity, PlayerEntity player) {
        final RenderEntityInvisibleEvent event = new RenderEntityInvisibleEvent(entity);
        Neverdies.EVENT_HANDLER.dispatch(event);
        if (event.isCanceled())
        {
            return false;
        }
        return entity.isInvisibleTo(player);
    }
}
