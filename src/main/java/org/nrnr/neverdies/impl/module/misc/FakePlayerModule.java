package org.nrnr.neverdies.impl.module.misc;

import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.impl.event.entity.player.PushEntityEvent;
import org.nrnr.neverdies.impl.event.network.DisconnectEvent;
import org.nrnr.neverdies.util.world.FakePlayerEntity;

/**
 * @author chronos
 * @see FakePlayerEntity
 * @since 1.0
 */
public class FakePlayerModule extends ToggleModule {
    //
    private FakePlayerEntity fakePlayer;

    /**
     *
     */
    public FakePlayerModule() {
        super("FakePlayer", "Spawns an indestructible client-side player",
                ModuleCategory.MISCELLANEOUS);
    }

    @Override
    public void onEnable() {
        if (mc.player != null && mc.world != null) {
            fakePlayer = new FakePlayerEntity(mc.player, "FakePlayer");
            fakePlayer.spawnPlayer();
        }
    }

    @Override
    public void onDisable() {
        if (fakePlayer != null) {
            fakePlayer.despawnPlayer();
            fakePlayer = null;
        }
    }

    @EventListener
    public void onDisconnect(DisconnectEvent event) {
        fakePlayer = null;
        disable();
    }

    @EventListener
    public void onPushEntity(PushEntityEvent event) {
        // Prevents Simulation flags (as the FakePlayer is client only, so Grim rightfully
        // flags us for that push motion that shouldn't happen
        if (event.getPushed().equals(mc.player) && event.getPusher().equals(fakePlayer)) {
            event.setCanceled(true);
        }
    }
}
