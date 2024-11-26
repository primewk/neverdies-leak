package org.nrnr.neverdies.mixin.gui.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import org.nrnr.neverdies.Neverdies;
import org.nrnr.neverdies.impl.event.gui.hud.RenderOverlayEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author chronos
 * @since 1.0
 */
@Mixin(InGameOverlayRenderer.class)
public class MixinInGameOverlayRenderer {
    /**
     * @param client
     * @param matrices
     * @param ci
     */
    @Inject(method = "renderFireOverlay", at = @At(value = "HEAD"),
            cancellable = true)
    private static void hookRenderFireOverlay(MinecraftClient client, MatrixStack matrices, CallbackInfo ci) {
        RenderOverlayEvent.Fire renderOverlayEvent =
                new RenderOverlayEvent.Fire(null);
        Neverdies.EVENT_HANDLER.dispatch(renderOverlayEvent);
        if (renderOverlayEvent.isCanceled()) {
            ci.cancel();
        }
    }

    /**
     * @param client
     * @param matrices
     * @param ci
     */
    @Inject(method = "renderUnderwaterOverlay", at = @At(value = "HEAD"),
            cancellable = true)
    private static void hookRenderUnderwaterOverlay(MinecraftClient client, MatrixStack matrices,
                                                    CallbackInfo ci) {
        RenderOverlayEvent.Water renderOverlayEvent =
                new RenderOverlayEvent.Water(null);
        Neverdies.EVENT_HANDLER.dispatch(renderOverlayEvent);
        if (renderOverlayEvent.isCanceled()) {
            ci.cancel();
        }
    }

    /**
     * @param sprite
     * @param matrices
     * @param ci
     */
    @Inject(method = "renderInWallOverlay", at = @At(value = "HEAD"),
            cancellable = true)
    private static void hookRenderFireOverlay(Sprite sprite, MatrixStack matrices, CallbackInfo ci) {
        RenderOverlayEvent.Block renderOverlayEvent =
                new RenderOverlayEvent.Block(null);
        Neverdies.EVENT_HANDLER.dispatch(renderOverlayEvent);
        if (renderOverlayEvent.isCanceled()) {
            ci.cancel();
        }
    }
}
