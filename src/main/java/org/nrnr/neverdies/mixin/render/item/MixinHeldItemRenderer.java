package org.nrnr.neverdies.mixin.render.item;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.NotNull;
import org.nrnr.neverdies.Neverdies;
import org.nrnr.neverdies.impl.event.render.item.RenderArmEvent;
import org.nrnr.neverdies.impl.event.render.item.RenderFirstPersonEvent;
import org.nrnr.neverdies.init.Modules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.nrnr.neverdies.util.Globals.mc;


@Mixin(HeldItemRenderer.class)
public class MixinHeldItemRenderer {

    @Shadow
    @Final
    private EntityRenderDispatcher entityRenderDispatcher;

    @Shadow
    @Final
    private MinecraftClient client;

    /**
     *
     * @param matrices
     * @param vertexConsumers
     * @param light
     * @param arm
     * @param ci
     */
    @Inject(method = "renderArmHoldingItem", at = @At(value = "HEAD"), cancellable = true)
    private void hookRenderArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float equipProgress, float swingProgress, Arm arm, CallbackInfo ci) {
        PlayerEntityRenderer playerEntityRenderer = (PlayerEntityRenderer) entityRenderDispatcher.getRenderer(client.player);
        RenderArmEvent renderArmEvent = new RenderArmEvent(matrices, vertexConsumers, light, equipProgress, swingProgress, arm, playerEntityRenderer);
        Neverdies.EVENT_HANDLER.dispatch(renderArmEvent);
        if (renderArmEvent.isCanceled()) {
            ci.cancel();
        }
    }

    /**
     * @param player
     * @param tickDelta
     * @param pitch
     * @param hand
     * @param swingProgress
     * @param item
     * @param equipProgress
     * @param matrices
     * @param vertexConsumers
     * @param light
     * @param ci
     */

    @Inject(method = "renderFirstPersonItem", at = @At(value = "RETURN"))
    private void onRenderItemPost(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (Modules.CHAMS_REWRITE.isEnabled() && Modules.CHAMS_REWRITE.handItems.getValue())
            RenderSystem.setShaderColor(1f,1f,1f,1f);
    }

    @Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
    private void onRenderItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        RenderFirstPersonEvent renderFirstPersonEvent = new RenderFirstPersonEvent(hand, item, equipProgress, matrices);
        Neverdies.EVENT_HANDLER.dispatch(renderFirstPersonEvent);
    }

    @Inject(method = "renderFirstPersonItem", at = @At(value = "HEAD"), cancellable = true)
    private void onRenderItemHook(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (Modules.SWING.shouldAnimate() && !(item.isEmpty()) && !(item.getItem() instanceof FilledMapItem)) {
            ci.cancel();
            Modules.SWING.renderFirstPersonItemCustom(player, tickDelta, pitch, hand, swingProgress, item, equipProgress, matrices, vertexConsumers, light);
        }
    }


    private void applyEatOrDrinkTransformationCustom(MatrixStack matrices, float tickDelta, Arm arm, @NotNull ItemStack stack) {
        float f = (float) mc.player.getItemUseTimeLeft() - tickDelta + 1.0F;
        float g = f / (float) stack.getMaxUseTime();
        float h;
        if (g < 0.8F) {
            h = MathHelper.abs(MathHelper.cos(f / 4.0F * 3.1415927F) * 0.005F);
            matrices.translate(0.0F, h, 0.0F);
        }
        h = 1.0F - (float) Math.pow(g, 27.0);
        int i = arm == Arm.RIGHT ? 1 : -1;

        matrices.translate(h * 0.6F * (float) i * 1, h * -0.5F * 1, h * 0.0F);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * h * 90.0F));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(h * 10.0F));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) i * h * 30.0F));
    }

    @Inject(method = "applyEatOrDrinkTransformation", at = @At(value = "HEAD"), cancellable = true)
    private void applyEatOrDrinkTransformationHook(MatrixStack matrices, float tickDelta, Arm arm, ItemStack stack, CallbackInfo ci) {
        if (Modules.SWING.isEnabled()) {
            applyEatOrDrinkTransformationCustom(matrices, tickDelta, arm, stack);
            ci.cancel();
        }
    }

//    @Inject(method = "applyEatOrDrinkTransformation", at = @At(value = "HEAD"), cancellable = true)
//    private void hookApplyEatOrDrinkTransformation(MatrixStack matrices, float tickDelta, Arm arm, ItemStack stack, CallbackInfo ci) {
//        ci.cancel();
//        float h;
//        float f = (float) client.player.getItemUseTimeLeft() - tickDelta + 1.0f;
//        float g = f / (float)stack.getMaxUseTime();
//        if (g < 0.8f) {
//            h = MathHelper.abs(MathHelper.cos(f / 4.0f * (float)Math.PI) * 0.1f);
//            matrices.translate(0.0f, h, 0.0f);
//        }
//        h = 1.0f - (float) Math.pow(g, 27.0);
//        int i = arm == Arm.RIGHT ? 1 : -1;
//        matrices.translate(h * 0.6f * (float)i, h * -0.5f, h * 0.0f);
//        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)i * h * 90.0f));
//        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(h * 10.0f));
//        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)i * h * 30.0f));
//    }
}
