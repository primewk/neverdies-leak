package org.nrnr.neverdies.impl.module.render;

import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.NumberConfig;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.impl.event.render.CameraClipEvent;

/**
 * @author chronos
 * @since 1.0
 */
public class ViewClipModule extends ToggleModule {

    Config<Float> distanceConfig = new NumberConfig<>("Distance", "The third-person camera clip distance", 1.0f, 3.5f, 20.0f);

    public ViewClipModule() {
        super("ViewClip", "Clips your third-person camera through blocks", ModuleCategory.RENDER);
    }

    @EventListener
    public void onCameraClip(CameraClipEvent event) {
        event.cancel();
        event.setDistance(distanceConfig.getValue());
    }
}
