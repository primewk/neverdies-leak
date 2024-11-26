package org.nrnr.neverdies.mixin.render.entity;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import org.nrnr.neverdies.Neverdies;
import org.nrnr.neverdies.impl.event.render.entity.RenderArmorEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author chronos
 * @see ArmorFeatureRenderer
 * @since 1.0
 */
@Mixin(ArmorFeatureRenderer.class)
public class MixinArmorFeatureRenderer {
    /**
     * @param matrices
     * @param vertexConsumers
     * @param entity
     * @param armorSlot
     * @param light
     * @param model
     * @param ci
     */
    @Inject(method = "renderArmor", at = @At(value = "HEAD"), cancellable = true)
    private void hookRenderArmor(MatrixStack matrices,
                                 VertexConsumerProvider vertexConsumers,
                                 LivingEntity entity, EquipmentSlot armorSlot,
                                 int light, BipedEntityModel<?> model, CallbackInfo ci) {
        RenderArmorEvent renderArmorEvent = new RenderArmorEvent(entity);
        Neverdies.EVENT_HANDLER.dispatch(renderArmorEvent);
        if (renderArmorEvent.isCanceled()) {
            ci.cancel();
        }
        ;
    }
}
