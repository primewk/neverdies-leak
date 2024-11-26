package org.nrnr.neverdies.mixin.entity.passive;

import net.minecraft.entity.passive.StriderEntity;
import org.nrnr.neverdies.Neverdies;
import org.nrnr.neverdies.impl.event.entity.passive.EntitySteerEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StriderEntity.class)
public class MixinStriderEntity {
    /**
     * @param cir
     */
    @Inject(method = "isSaddled", at = @At(value = "HEAD"), cancellable = true)
    private void hookIsSaddled(CallbackInfoReturnable<Boolean> cir) {
        EntitySteerEvent entitySteerEvent = new EntitySteerEvent();
        Neverdies.EVENT_HANDLER.dispatch(entitySteerEvent);
        if (entitySteerEvent.isCanceled()) {
            cir.cancel();
            cir.setReturnValue(true);
        }
    }
}
