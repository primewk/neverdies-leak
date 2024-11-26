package org.nrnr.neverdies.impl.event.item;

import org.nrnr.neverdies.api.event.Cancelable;
import org.nrnr.neverdies.api.event.Event;

@Cancelable
public class DurabilityEvent extends Event {
    //
    private int damage;

    public DurabilityEvent(int damage) {
        this.damage = damage;
    }

    public int getItemDamage() {
        return Math.max(0, damage);
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }
}
