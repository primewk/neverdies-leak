package org.nrnr.neverdies.mixin.render.entity;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.nrnr.neverdies.Neverdies;
import org.nrnr.neverdies.impl.event.render.entity.RenderLabelEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author chronos
 * @since 1.0
 */
@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

    /**
     * @param entity
     * @param text
     * @param matrices
     * @param vertexConsumers
     * @param light
     * @param ci
     */
    @Inject(method = "renderLabelIfPresent", at = @At(value = "HEAD"),
            cancellable = true)
    public void hookRenderLabelIfPresent(Entity entity, Text text, MatrixStack matrices,
                                         VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        RenderLabelEvent renderLabelEvent = new RenderLabelEvent(entity);
        Neverdies.EVENT_HANDLER.dispatch(renderLabelEvent);
        if (renderLabelEvent.isCanceled()) {
            ci.cancel();
        }
    }
}
