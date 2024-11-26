package org.nrnr.neverdies.impl.module.legit;

import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.config.setting.EnumConfig;
import org.nrnr.neverdies.api.config.setting.NumberConfig;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.impl.event.gui.hud.ChatTextEvent;
import org.nrnr.neverdies.init.Modules;
import org.nrnr.neverdies.util.render.animation.Easing;
import org.nrnr.neverdies.util.render.animation.TimeAnimation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

//TODO: add easing when linus fixes enumconfig...
public class BetterChatModule extends ToggleModule {
    Config<Timestamp> timestampConfig = new EnumConfig<>("Timestamp", "Shows chat timestamps", Timestamp.OFF, Timestamp.values());
    Config<Boolean> animationConfig = new BooleanConfig("Animation", "Animates the chat", false);
    Config<Integer> timeConfig = new NumberConfig<>("Anim-Time", "Time for the animation", 0, 200, 1000, () -> false);
    Config<Boolean> noSignatureConfig = new BooleanConfig("NoSignatureIndicator", "Removes the message signature indicator", false);

    public final Map<ChatHudLine, TimeAnimation> animationMap = new HashMap<>();

    public BetterChatModule() {
        super("BetterChat", "Modifications for the chat", ModuleCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onChatText(ChatTextEvent event) {
        if (timestampConfig.getValue() != Timestamp.OFF) {
            String time = new SimpleDateFormat("k:mm").format(new Date());
            OrderedText text = switch (timestampConfig.getValue()) {
                case NORMAL ->
                        OrderedText.concat(fromString("<", Style.EMPTY.withColor(Formatting.DARK_GRAY)), fromString(time, Style.EMPTY.withColor(Formatting.GRAY)), fromString("> ", Style.EMPTY.withColor(Formatting.DARK_GRAY)));
                case COLOR ->
                        OrderedText.concat(fromString("<" + time + "> ", Style.EMPTY.withColor(Modules.COLORS.getRGB())));
                case OFF -> OrderedText.EMPTY;
            };
            event.cancel();
            event.setText(OrderedText.concat(text, event.getText()));
        }
    }

    private OrderedText fromString(String string, Style style) {
        return OrderedText.styledForwardsVisitedString(string, style);
    }

    public Config<Boolean> getAnimationConfig() {
        return animationConfig;
    }

    public Config<Integer> getTimeConfig() {
        return timeConfig;
    }

    /*
    public Config<Easing> getEasingConfig()
    {
        return easing;
    }
    */

    public Easing getEasingConfig() {
        return Easing.LINEAR;
    }

    public Config<Boolean> getNoSignatureConfig() {
        return noSignatureConfig;
    }

    public enum Timestamp {
        NORMAL,
        COLOR,
        OFF
    }
}
