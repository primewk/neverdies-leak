package org.nrnr.neverdies.mixin.gui.hud;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.nrnr.neverdies.Neverdies;
import org.nrnr.neverdies.impl.event.gui.hud.ChatMessageEvent;
import org.nrnr.neverdies.impl.event.gui.hud.ChatTextEvent;
import org.nrnr.neverdies.impl.imixin.IChatHud;
import org.nrnr.neverdies.init.Modules;
import org.nrnr.neverdies.util.render.animation.TimeAnimation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * @author chronos
 * @since 1.0
 */
@Mixin(ChatHud.class)
public abstract class MixinChatHud implements IChatHud {
    @Shadow
    @Final
    private List<ChatHudLine> messages;
    @Shadow
    @Final
    private List<ChatHudLine.Visible> visibleMessages;

    @Shadow
    public abstract void addMessage(Text message);

    private ChatHudLine current = null;
    private int currentId;

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/ChatHudLine$Visible;addedTime()I"))
    private void hookTimeAdded(CallbackInfo ci, @Local(ordinal = 13) int chatLineIndex) {
        try {
            current = messages.get(chatLineIndex);
        } catch (Exception ignored) {

        }
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;" +
                            "drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;" +
                            "Lnet/minecraft/text/OrderedText;III)I"))
    private int drawTextWithShadowHook(DrawContext instance,
                                       TextRenderer textRenderer,
                                       OrderedText text,
                                       int x,
                                       int y,
                                       int color) {
        TimeAnimation animation = null;
        if (current != null) {
            if (Modules.BETTER_CHAT.animationMap.containsKey(current)) {
                animation = Modules.BETTER_CHAT.animationMap.get(current);
            }
        }

        if (animation != null) {
            animation.setState(true);
        }

        ChatTextEvent chatTextEvent = new ChatTextEvent(text);
        Neverdies.EVENT_HANDLER.dispatch(chatTextEvent);
        if (chatTextEvent.isCanceled()) {
            return instance.drawTextWithShadow(textRenderer, chatTextEvent.getText(), (int) ((animation != null && Modules.BETTER_CHAT.isEnabled() && Modules.BETTER_CHAT.getAnimationConfig().getValue() ? animation.getCurrent() : 0)), y, color);
        }
        return instance.drawTextWithShadow(textRenderer, text, (int) ((animation != null && Modules.BETTER_CHAT.isEnabled() && Modules.BETTER_CHAT.getAnimationConfig().getValue() ? animation.getCurrent() : 0)), y, color);
    }

    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/" +
            "ChatHudLine$Visible;indicator()Lnet/minecraft/client/gui/hud/MessageIndicator;"))
    private MessageIndicator hookRender(MessageIndicator original) {
        return Modules.BETTER_CHAT.getNoSignatureConfig().getValue() ? null : original;
    }

    /**
     * @param message
     * @param signature
     * @param ticks
     * @param indicator
     * @param refresh
     * @param ci
     */
    @Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/" +
            "network/message/MessageSignatureData;ILnet/minecraft/client/" +
            "gui/hud/MessageIndicator;Z)V", at = @At(value = "HEAD"))
    private void hookAddMessage(Text message, MessageSignatureData signature,
                                int ticks, MessageIndicator indicator,
                                boolean refresh, CallbackInfo ci) {
        ChatMessageEvent chatMessageEvent = new ChatMessageEvent(message);
        Neverdies.EVENT_HANDLER.dispatch(chatMessageEvent);
    }

    @Override
    public void addMessage(String message, int id) {
        currentId = id;
        addMessage(Text.of(message));
        currentId = -1;
    }
}
