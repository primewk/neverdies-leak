package org.nrnr.neverdies;

import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.discord.DiscordEventHandlers;
import org.nrnr.neverdies.api.discord.DiscordRPC;
import org.nrnr.neverdies.api.discord.DiscordRichPresence;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.nrnr.neverdies.util.Globals.mc;


public class RPC {
    public static String discordID = "1259952133025959976";
    public static org.nrnr.neverdies.api.discord.DiscordRichPresence discordRichPresence = new DiscordRichPresence();
    public static DiscordRPC discordRPC = DiscordRPC.INSTANCE;
    public static Config<Boolean> showIP = new BooleanConfig("ShowIP","sso", true);

    public static void startRPC() {

        String pastebinUrl = "https://pastebin.com/raw/CnafMAJY";
        List<Integer> lineNumbers = new ArrayList<>();
        List<String> hwid = new ArrayList<>();
        int uid = -1;

        try {
            URL url = new URL(pastebinUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                int lineNumber = 1;

                while ((line = reader.readLine()) != null) {
                    lineNumbers.add(lineNumber);
                    hwid.add(line);
                    lineNumber++;

                    if (line.trim().equals(NeverdiesMod.getHWID())) {
                        uid = lineNumber - 1;
                    }
                }

                reader.close();
            } else {
            }

            connection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }


        DiscordEventHandlers eventHandlers = new DiscordEventHandlers();
        eventHandlers.disconnected = RPC::lambda$startRPC$0;
        discordRPC.Discord_Initialize(discordID, eventHandlers, true, null);
        RPC.discordRichPresence.startTimestamp = System.currentTimeMillis() / ((long)-2121370231 ^ 0xFFFFFFFF818E7661L);
        RPC.discordRichPresence.details = mc.getSession().getUsername() + " | UID: " + uid ;
        RPC.discordRichPresence.largeImageKey = "neverdies";
        //RPC.discordRichPresence.largeImageKey = mc.player.networkHandler.getServerInfo().address;
        RPC.discordRichPresence.largeImageText = NeverdiesMod.MOD_VER;
        //RPC.discordRichPresence.state = "by Chronos + Mortex" ;
        discordRPC.Discord_UpdatePresence(discordRichPresence);
    }
    public static void stopRPC() {
        discordRPC.Discord_Shutdown();
        discordRPC.Discord_ClearPresence();
    }

    public static void lambda$startRPC$0(final int var1, final String var2) {
        System.out.println("Discord RPC disconnected, var1: " + var1 + ", var2: " + var2);
    }



}