package org.nrnr.neverdies.impl.module.render;

import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.biome.BiomeParticleConfig;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.EnumConfig;
import org.nrnr.neverdies.api.event.EventStage;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.impl.event.TickEvent;
import org.nrnr.neverdies.impl.event.biome.BiomeEffectsEvent;
import org.nrnr.neverdies.impl.event.network.PacketEvent;
import org.nrnr.neverdies.util.string.EnumFormatter;

/**
 * @author chronos
 * @since 1.0
 */
public class NoWeatherModule extends ToggleModule {

    Config<Weather> weatherConfig = new EnumConfig<>("Weather", "The world weather", Weather.CLEAR, Weather.values());
    // The current weather mode
    private Weather weather;

    public NoWeatherModule() {
        super("NoWeather", "Prevents weather rendering", ModuleCategory.RENDER);
    }

    @Override
    public String getModuleData() {
        return EnumFormatter.formatEnum(weatherConfig.getValue());
    }

    @Override
    public void onEnable() {
        if (mc.world != null) {
            if (mc.world.isThundering()) {
                weather = Weather.THUNDER;
            } else if (mc.world.isRaining()) {
                weather = Weather.RAIN;
            } else {
                weather = Weather.CLEAR;
            }
            setWeather(weatherConfig.getValue());
        }
    }

    @Override
    public void onDisable() {
        if (mc.world != null && weather != null) {
            setWeather(weather);
        }
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() == EventStage.POST) {
            setWeather(weatherConfig.getValue());
        }
    }

    @EventListener
    public void onBiomeEffects(BiomeEffectsEvent event) {
        if (weatherConfig.getValue() == Weather.ASH) {
            event.cancel();
            event.setParticleConfig(new BiomeParticleConfig(ParticleTypes.WHITE_ASH, 0.118093334f));
        }
    }

    private void setWeather(Weather weather) {
        switch (weather) {
            case CLEAR, ASH -> {
                mc.world.getLevelProperties().setRaining(false);
                mc.world.setRainGradient(0.0f);
                mc.world.setThunderGradient(0.0f);
            }
            case RAIN -> {
                mc.world.getLevelProperties().setRaining(true);
                mc.world.setRainGradient(1.0f);
                mc.world.setThunderGradient(0.0f);
            }
            case THUNDER -> {
                mc.world.getLevelProperties().setRaining(true);
                mc.world.setRainGradient(2.0f);
                mc.world.setThunderGradient(1.0f);
            }
        }
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event) {
        if (event.getPacket() instanceof GameStateChangeS2CPacket packet) {
            if (packet.getReason() == GameStateChangeS2CPacket.RAIN_STARTED
                    || packet.getReason() == GameStateChangeS2CPacket.RAIN_STOPPED
                    || packet.getReason() == GameStateChangeS2CPacket.RAIN_GRADIENT_CHANGED
                    || packet.getReason() == GameStateChangeS2CPacket.THUNDER_GRADIENT_CHANGED) {
                event.cancel();
            }
        }
    }

    public enum Weather {
        CLEAR,
        RAIN,
        THUNDER,
        ASH
    }
}
