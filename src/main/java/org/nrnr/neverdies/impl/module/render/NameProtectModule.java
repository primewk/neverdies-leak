package org.nrnr.neverdies.impl.module.render;

import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.StringConfig;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.impl.event.text.TextVisitEvent;

/**
 * @author chronos
 * @since 1.0
 */
public class NameProtectModule extends ToggleModule {

    Config<String> placeholderConfig = new StringConfig("Placeholder", "The placeholder name for the player", "Player");

    public NameProtectModule() {
        super("NameProtect", "Hides the player name in chat and tablist",
                ModuleCategory.RENDER);
    }

    @EventListener
    public void onTextVisit(TextVisitEvent event) {
        if (mc.player == null) {
            return;
        }
        final String username = mc.getSession().getUsername();
        final String text = event.getText();
        if (text.contains(username)) {
            event.cancel();
            event.setText(text.replace(username, placeholderConfig.getValue()));
        }
    }
}
