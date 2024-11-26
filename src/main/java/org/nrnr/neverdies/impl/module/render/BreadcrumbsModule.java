package org.nrnr.neverdies.impl.module.render;

import net.minecraft.util.math.Vec3d;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.config.setting.NumberConfig;
import org.nrnr.neverdies.api.event.EventStage;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.impl.event.network.PlayerUpdateEvent;
import org.nrnr.neverdies.impl.event.render.RenderWorldEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author chronos
 * @since 1.0
 */
public class BreadcrumbsModule extends ToggleModule {

    private final Map<Vec3d, Long> positions = new ConcurrentHashMap<>();
    Config<Boolean> infiniteConfig = new BooleanConfig("Infinite", "Renders breadcrumbs for all positions since toggle", true);
    Config<Float> maxTimeConfig = new NumberConfig<>("MaxPosition", "The maximum time for a given position", 1.0f, 2.0f, 20.0f);

    public BreadcrumbsModule() {
        super("Breadcrumbs", "Renders a line connecting all previous positions", ModuleCategory.RENDER);
    }

    @Override
    public void onDisable() {
        positions.clear();
    }

    @EventListener
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (event.getStage() != EventStage.PRE) {
            return;
        }
        positions.put(new Vec3d(mc.player.getX(), mc.player.getBoundingBox().minY, mc.player.getZ()), System.currentTimeMillis());
        if (!infiniteConfig.getValue()) {
            positions.forEach((p, t) ->
            {
                if (System.currentTimeMillis() - t >= maxTimeConfig.getValue() * 1000) {
                    positions.remove(p);
                }
            });
        }
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event) {

    }
}
