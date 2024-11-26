package org.nrnr.neverdies.impl.module.render;

import net.minecraft.entity.player.PlayerEntity;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.impl.event.render.entity.RenderEntityInvisibleEvent;

/**
 * @author xgraza
 * @since 1.0
 */
public final class TrueSightModule extends ToggleModule {
    Config<Boolean> onlyPlayersConfig = new BooleanConfig("OnlyPlayers", "If to only reveal invisible players", true);

    public TrueSightModule() {
        super("TrueSight", "Allows you to see invisible entities", ModuleCategory.RENDER);
    }

    @EventListener
    public void onRenderEntityInvisible(final RenderEntityInvisibleEvent event) {
        if (event.getEntity().isInvisible() && (!onlyPlayersConfig.getValue() || event.getEntity() instanceof PlayerEntity)) {
            event.cancel();
        }
    }
}
