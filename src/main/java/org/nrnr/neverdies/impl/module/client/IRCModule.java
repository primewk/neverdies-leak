package org.nrnr.neverdies.impl.module.client;

import net.minecraft.text.Text;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.BooleanConfig;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.impl.event.gui.chat.ChatMessageEvent;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chronos
 * @since 1.0
 */
public class IRCModule extends ToggleModule {
    private static final String SERVER_ADDRESS = "localhost"; // Adjust as necessary
    private static final int SERVER_PORT = 12345; // Adjust as necessary

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread listenerThread;

    /**
     *
     */
    public IRCModule() {
        super("IRC", "Changes the client text to custom font rendering", ModuleCategory.CLIENT);
    }

    @Override
    public void onEnable() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Connected to IRC server.");

            listenerThread = new Thread(this::listenForMessages);
            listenerThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            System.out.println("Disconnected from IRC server.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void listenForMessages() {
        try {
            String serverMessage;
            while ((serverMessage = in.readLine()) != null) {

                System.out.println("Received: " + serverMessage);
                mc.inGameHud.getChatHud().addMessage(Text.of(serverMessage), null, null);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Path userHomePath = Paths.get(System.getProperty("user.home"));
    Path folderPath = userHomePath.resolve("a");
    File aTxtFile = folderPath.resolve("a.txt").toFile();


    private List<String> readLinesFromFile(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.lines().collect(Collectors.toList());
        }
    }

    @EventListener
    public void onChatMessage(ChatMessageEvent.Client event) throws IOException {
        final String text = event.getMessage().trim();
        if (text.startsWith("%")) {
            event.cancel();

            String messageToSend = text.substring(1);

            if (out != null) {
                out.println(readLinesFromFile(aTxtFile) + " " + messageToSend);
            }
        }
    }


}
