package org.nrnr.neverdies.impl.event.entity.player;

import net.minecraft.util.math.Vec3d;
import org.nrnr.neverdies.api.event.Cancelable;
import org.nrnr.neverdies.api.event.StageEvent;

@Cancelable
public class TravelEvent extends StageEvent {
    private final Vec3d movementInput;

    public TravelEvent(Vec3d movementInput) {
        this.movementInput = movementInput;
    }

    public Vec3d getMovementInput() {
        return movementInput;
    }
}
