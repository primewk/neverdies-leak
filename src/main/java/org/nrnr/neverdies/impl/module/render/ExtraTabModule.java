package org.nrnr.neverdies.impl.module.render;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.config.setting.NumberConfig;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.impl.event.gui.hud.PlayerListColumnsEvent;
import org.nrnr.neverdies.impl.event.gui.hud.PlayerListEvent;
import org.nrnr.neverdies.impl.event.gui.hud.PlayerListNameEvent;
import org.nrnr.neverdies.init.Managers;

/**
 * @author hockeyl8, linus
 * @since 1.0
 */
public class ExtraTabModule extends ToggleModule {

    Config<Integer> sizeConfig = new NumberConfig<>("Size", "The number of players to show", 80, 200, 1000);
    Config<Integer> columnsConfig = new NumberConfig<>("Columns", "The number columns to show.", 1, 20, 100);
    Config<Boolean> selfConfig = new BooleanConfig("Self", "Highlights yourself in the tab list.", false);
    Config<Boolean> friendsConfig = new BooleanConfig("Friends", "Highlights friends in the tab list.", true);

    public ExtraTabModule() {
        super("ExtraTab", "Expands the tab list size to allow for more players", ModuleCategory.RENDER);
    }

    @EventListener
    public void onPlayerListName(PlayerListNameEvent event) {
        if (selfConfig.getValue() && event.getPlayerName().getString().contains(mc.getGameProfile().getName())) {
            event.cancel();
            event.setPlayerName(Text.of(("§s" + event.getPlayerName().getString())));
        } else if (friendsConfig.getValue()) {
            for (String s : Managers.SOCIAL.getFriends()) {
                if (event.getPlayerName().getString().contains(s)) {
                    event.cancel();
                    event.setPlayerName(Text.of(Formatting.AQUA + event.getPlayerName().getString()));
                    break;
                }
            }
        }
    }

    @EventListener
    public void onPlayerList(PlayerListEvent event) {
        event.cancel();
        event.setSize(sizeConfig.getValue());
    }

    @EventListener
    public void onPlayerListColumns(PlayerListColumnsEvent event) {
        event.cancel();
        event.setTabHeight(columnsConfig.getValue());
    }
}
