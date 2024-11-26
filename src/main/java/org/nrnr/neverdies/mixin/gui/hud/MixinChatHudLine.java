package org.nrnr.neverdies.mixin.gui.hud;

import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import org.nrnr.neverdies.impl.imixin.IChatHudLine;
import org.nrnr.neverdies.init.Modules;
import org.nrnr.neverdies.util.Globals;
import org.nrnr.neverdies.util.render.animation.Easing;
import org.nrnr.neverdies.util.render.animation.TimeAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHudLine.class)
public abstract class MixinChatHudLine implements IChatHudLine, Globals {
    @Unique
    private int id;

    /**
     * Change "Modules.BETTER_CHAT.getEasingConfig())" to
     * Modules.BETTER_CHAT.getEasingConfig().getValue())
     * when linus fixes enumconfig!
     */
    @Inject(
            method = "<init>",
            at = @At(value = "RETURN"))
    private void hookCtr(int creationTick,
                         Text text,
                         MessageSignatureData messageSignatureData,
                         MessageIndicator messageIndicator,
                         CallbackInfo info) {
        Modules.BETTER_CHAT.animationMap.put(
                ChatHudLine.class.cast(this),
                new TimeAnimation(false,
                        -mc.textRenderer.getWidth(text.getString()),
                        0,
                        Modules.BETTER_CHAT.getTimeConfig().getValue(),
                        Modules.BETTER_CHAT.getEasingConfig()));
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }
}
