package org.nrnr.neverdies.impl.event.network;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.nrnr.neverdies.api.event.Event;
import org.nrnr.neverdies.mixin.network.MixinClientPlayerEntity;
import org.nrnr.neverdies.util.Globals;

/**
 * @author chronos
 * @see MixinClientPlayerEntity
 * @since 1.0
 */
public class SetCurrentHandEvent extends Event implements Globals {
    //
    private final Hand hand;

    public SetCurrentHandEvent(Hand hand) {
        this.hand = hand;
    }

    public Hand getHand() {
        return hand;
    }

    public ItemStack getStackInHand() {
        return mc.player.getStackInHand(hand);
    }
}
