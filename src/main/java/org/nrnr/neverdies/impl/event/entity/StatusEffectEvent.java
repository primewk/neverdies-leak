package org.nrnr.neverdies.impl.event.entity;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.nrnr.neverdies.api.event.Event;

public class StatusEffectEvent extends Event {

    private final StatusEffectInstance statusEffectInstance;

    public StatusEffectEvent(StatusEffectInstance statusEffectInstance) {
        this.statusEffectInstance = statusEffectInstance;
    }

    public StatusEffectInstance getStatusEffect() {
        return statusEffectInstance;
    }

    public static class Add extends StatusEffectEvent {

        public Add(StatusEffectInstance statusEffectInstance) {
            super(statusEffectInstance);
        }
    }

    public static class Remove extends StatusEffectEvent {

        public Remove(StatusEffectInstance statusEffectInstance) {
            super(statusEffectInstance);
        }
    }
}
