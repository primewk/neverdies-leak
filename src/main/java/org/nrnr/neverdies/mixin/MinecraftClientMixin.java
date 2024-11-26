package org.nrnr.neverdies.mixin;


import net.minecraft.client.MinecraftClient;
import org.nrnr.neverdies.NeverdiesMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @ModifyArg(method = "updateWindowTitle", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;setTitle(Ljava/lang/String;)V"))
    public String updateWindowTitleInvoke$setTitle(String title){
        return NeverdiesMod.MOD_NAME + " " + NeverdiesMod.MOD_VER + "+aFexV6FcdZ";
    }

}