package org.nrnr.neverdies.impl.module.render;

import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.MathHelper;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.config.setting.ColorConfig;
import org.nrnr.neverdies.api.config.setting.EnumConfig;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.impl.event.particle.ParticleEvent;
import org.nrnr.neverdies.impl.event.particle.TotemParticleEvent;

import java.awt.*;

/**
 * @author Neverdies
 * @since 1.0
 */
public class ParticlesModule extends ToggleModule {

    Config<TotemParticle> totemConfig = new EnumConfig<>("Totem", "Renders totem particles", TotemParticle.OFF, TotemParticle.values());
    Config<Color> totemColorConfig = new ColorConfig("TotemColor", "Color of the totem particles", new Color(25, 120, 0), false, false, () -> totemConfig.getValue() == TotemParticle.COLOR);
    Config<Boolean> fireworkConfig = new BooleanConfig("Firework", "Renders firework particles", false);
    Config<Boolean> potionConfig = new BooleanConfig("Effects", "Renders potion effect particles", true);
    Config<Boolean> bottleConfig = new BooleanConfig("BottleSplash", "Render bottle splash particles", true);
    Config<Boolean> portalConfig = new BooleanConfig("Portal", "Render portal particles", true);

    public ParticlesModule() {
        super("Particles", "Change the rendering of particles", ModuleCategory.RENDER);
    }

    @EventListener
    public void onParticle(ParticleEvent event) {
        if (potionConfig.getValue() && event.getParticleType() == ParticleTypes.ENTITY_EFFECT
                || fireworkConfig.getValue() && event.getParticleType() == ParticleTypes.FIREWORK
                || bottleConfig.getValue() && (event.getParticleType() == ParticleTypes.EFFECT || event.getParticleType() == ParticleTypes.INSTANT_EFFECT)
                || portalConfig.getValue() && event.getParticleType() == ParticleTypes.PORTAL) {
            event.cancel();
        }
    }

    @EventListener
    public void onTotemParticle(TotemParticleEvent event) {
        if (totemConfig.getValue() == TotemParticle.COLOR) {
            event.cancel();
            Color color = totemColorConfig.getValue();
            float r = color.getRed() / 255.0f;
            float g = color.getGreen() / 255.0f;
            float b = color.getBlue() / 255.0f;
            event.setColor(new Color(MathHelper.clamp(r + RANDOM.nextFloat() * 0.1f, 0.0f, 1.0f),
                    MathHelper.clamp(g + RANDOM.nextFloat() * 0.1f, 0.0f, 1.0f),
                    MathHelper.clamp(b + RANDOM.nextFloat() * 0.1f, 0.0f, 1.0f)));
        }
    }

    @EventListener
    public void onParticleEmitter(ParticleEvent.Emitter event) {
        if (totemConfig.getValue() == TotemParticle.REMOVE && event.getParticleType() == ParticleTypes.TOTEM_OF_UNDYING) {
            event.cancel();
        }
    }

    private enum TotemParticle {
        OFF,
        REMOVE,
        COLOR
    }
}
