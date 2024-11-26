package org.nrnr.neverdies;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import org.apache.commons.codec.digest.DigestUtils;
import org.nrnr.neverdies.auth.utils.NetworkUtil;
import org.nrnr.neverdies.util.world.CardinalDirection;
import org.nrnr.neverdies.util.world.con1;

import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.NetPermission;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.nrnr.neverdies.util.Globals.mc;

public class NeverdiesMod implements ClientModInitializer {
    public static final String MOD_NAME = "Neverdies";
    public static final String MOD_VER = "1.0.7";
    public static int finaluid = -1;

    public void startLoader(String jarname, String version) {
        String urlString = "https://pastebin.com/raw/x1yqyRsw";
        String newversion = read(urlString);

        if (!NetworkUtil.isInternetAvailable()){
            JFrame j2 = new JFrame();
            j2.setAlwaysOnTop(true);
            j2.setVisible(false);
            JOptionPane.showMessageDialog(j2, "Turn on Wifi", "Neverdies Loader", JOptionPane.INFORMATION_MESSAGE);
            mc.close();
            this.crash();
        }

        if (!Objects.equals(version, newversion)) {
            File file = Paths.get(FabricLoader.getInstance().getGameDir().toString(), "mods", jarname).toFile();
            File modsDir = Paths.get(FabricLoader.getInstance().getGameDir().toString(), "mods", jarname).toFile();

            for (File deleteFile : Objects.requireNonNull(modsDir.listFiles())) {
                if (deleteFile.getName().contains("neverdies")) {
                    deleteFile.delete();
                    System.out.println("deleted " + deleteFile.getName());
                }
            }

            try (BufferedInputStream bis = new BufferedInputStream(new URL("https://qrcd.org/6cQm").openStream());
                 FileOutputStream fos = new FileOutputStream(file)) {
                int b;
                byte[] db = new byte[1024];
                while ((b = bis.read(db, 0, 1024)) != -1) {
                    fos.write(db, 0, b);
                }
                JFrame j = new JFrame();
                j.setAlwaysOnTop(true);
                j.setVisible(false);
                JOptionPane.showMessageDialog(j, "The Loader has been Updated, Relaunch your Game", "Neverdies Loader", JOptionPane.INFORMATION_MESSAGE);

                SwingUtilities.invokeLater(this::crash);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void antiDecrypt(){
    }

    public void startLogin() throws IOException {
        Path userHomePath = Paths.get(System.getProperty("user.home"));
        Path folderPath = userHomePath.resolve("a");

        if (!folderPath.toFile().exists()) {
            folderPath.toFile().mkdir();
        }

        File aTxtFile = folderPath.resolve("a.txt").toFile();
        File bTxtFile = folderPath.resolve("b.txt").toFile();

        if (!aTxtFile.exists()) {
            aTxtFile.createNewFile();
        }

        if (!bTxtFile.exists()) {
            bTxtFile.createNewFile();
        }

        if (areCredValidFile()) {
            skip();
        } else {
            clearFileContents(aTxtFile);
            clearFileContents(bTxtFile);

            showLoginScreen();
        }
    }

    private void clearFileContents(File file) {
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.print("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean areCredValidFile() throws IOException {
        Path userHomePath = Paths.get(System.getProperty("user.home"));
        Path folderPath = userHomePath.resolve("a");
        File aTxtFile = folderPath.resolve("a.txt").toFile();
        File bTxtFile = folderPath.resolve("b.txt").toFile();

        List<String> usernames = readLinesFromFile(aTxtFile);
        List<String> passwords = readLinesFromFile(bTxtFile);

        for (String username : usernames) {
            for (String password : passwords) {
                if (areCredValid(username, password)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void add(File file, String line) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {
            writer.println(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showLoginScreen() throws IOException {
        JFrame frame = new JFrame("Login");
        frame.setAlwaysOnTop(true);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JTextField usernameField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);

        int result = JOptionPane.showConfirmDialog(frame, panel, " Log In to Neverdies Client", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (areCredValid(username, password)) {
                JOptionPane.showMessageDialog(frame, "Login Successful! | neverdies.me", "Neverdies Loader", JOptionPane.INFORMATION_MESSAGE);

                Path userHomePath = Paths.get(System.getProperty("user.home"));
                Path folderPath = userHomePath.resolve("a");
                File aTxtFile = folderPath.resolve("a.txt").toFile();
                File bTxtFile = folderPath.resolve("b.txt").toFile();
                add(aTxtFile, username);
                add(bTxtFile, password);

            } else {
                JOptionPane.showMessageDialog(frame, "Invalid credentials. Please try again.", "Neverdies Loader", JOptionPane.ERROR_MESSAGE);
                showLoginScreen();
            }
        } else {
            startLogin();
        }
    }


    public boolean areCredValid(String username, String password) {
        String loginUrl = "http://neverdies.nont123.nl/api/login";

        String jsonInputString = String.format(
                "{\"username\":\"%s\",\"password\":\"%s\"}",
                username, password
        );

        try {
            URL obj = new URL(loginUrl);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = con.getResponseCode();

            StringBuilder response = new StringBuilder();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
                String responseLine;
                while ((responseLine = in.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            String responseString = response.toString();
            String token = extract(responseString);
            if (token != null) {
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private String extract(String json) {
        String tokenKey = "\"token\":\"";
        int tokenStartIndex = json.indexOf(tokenKey);

        if (tokenStartIndex == -1) {
            return null;
        }

        tokenStartIndex += tokenKey.length();
        int tokenEndIndex = json.indexOf("\"", tokenStartIndex);

        if (tokenEndIndex == -1) {
            return null;
        }

        return json.substring(tokenStartIndex, tokenEndIndex);
    }

    private String extractID(String json) {
        String tokenKey = "\"id\":\"";
        int tokenStartIndex = json.indexOf(tokenKey);

        if (tokenStartIndex == -1) {
            return null;
        }

        tokenStartIndex += tokenKey.length();
        int tokenEndIndex = json.indexOf("\"", tokenStartIndex);

        if (tokenEndIndex == -1) {
            return null;
        }

        return json.substring(tokenStartIndex, tokenEndIndex);
    }


    private List<String> readLinesFromFile(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.lines().collect(Collectors.toList());
        }
    }

    private void skip() {
    }

    public void crash() {
        MinecraftClient.getInstance().stop();
    }

    public static String read(String urlString) {
        StringBuilder result = new StringBuilder();
        try {
            URL url = new URL(urlString);
            try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
                String line;
                while ((line = in.readLine()) != null) {
                    result.append(line).append(System.lineSeparator());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString().trim();
    }

    public static String getHWID() {
        return DigestUtils.sha3_256Hex(DigestUtils.md2Hex
                (DigestUtils.sha512Hex(DigestUtils.sha512Hex
                        (System.getenv("os") + System.getProperty("os.name")
                                + System.getProperty("os.arch") + System.getProperty("os.version")
                                + System.getProperty("user.language") + System.getenv("SystemRoot")
                                + System.getenv("HOMEDRIVE") + System.getenv("PROCESSOR_LEVEL")
                                + System.getenv("PROCESSOR_REVISION") + System.getenv("PROCESSOR_IDENTIFIER")
                                + System.getenv("PROCESSOR_ARCHITECTURE") + System.getenv("PROCESSOR_ARCHITEW6432")
                                + System.getenv("NUMBER_OF_PROCESSORS")))));
    }

    private String getFirst(File file) {
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            if (!lines.isEmpty()) {
                return lines.get(0).trim();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    String token = null;
    String uid = null;

    @Override
    public void onInitializeClient() {
        String loginUrl = "http://neverdies.nont123.nl/api/login";


        Path userHomePath = Paths.get(System.getProperty("user.home"));
        Path folderPath = userHomePath.resolve("a");
        File aTxtFile = folderPath.resolve("a.txt").toFile();
        File bTxtFile = folderPath.resolve("b.txt").toFile();

        if (!aTxtFile.exists() | !bTxtFile.exists()) {
            try {
                startLogin();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        boolean hwidallowed = false;
        if (aTxtFile.exists() && bTxtFile.exists()) {

            String username = getFirst(aTxtFile);
            String password = getFirst(bTxtFile);


            String jsonInputString = String.format(
                    "{\"username\":\"%s\",\"password\":\"%s\"}",
                    username, password
            );

            try {
                URL obj = new URL(loginUrl);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setDoOutput(true);

                try (OutputStream os = con.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = con.getResponseCode();

                StringBuilder response = new StringBuilder();
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
                    String responseLine;
                    while ((responseLine = in.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                }

                String responseString = response.toString();
                token = extract(responseString);


            } catch (Exception e) {
                e.printStackTrace();
            }

            String hwid = getHWID();
            String url2 = "http://neverdies.nont123.nl/api/verify/" + hwid;
            hwidallowed = false;
            try {
                URL obj = new URL(url2);
                HttpURLConnection con1 = (HttpURLConnection) obj.openConnection();

                con1.setRequestMethod("GET");
                con1.setRequestProperty("Content-Type", "application/json");

                con1.setRequestProperty("Authorization", token);
                int responseCode = con1.getResponseCode();
                System.out.println("Response Code: " + responseCode);

                StringBuilder response1 = new StringBuilder();
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con1.getInputStream(), "utf-8"))) {
                    String responseLine;
                    while ((responseLine = in.readLine()) != null) {
                        response1.append(responseLine.trim());
                    }
                }

                String responseBody = response1.toString();
                System.out.println("Response Body: " + responseBody);

                if (responseBody.contains("success")) {
                    hwidallowed = true;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (hwidallowed) {
                try {
                    String webhookUrl1 = "https://discord.com/api/webhooks/1292544301779255418/hCNvzeYKKbuFN1RwrTc5OK1uVa3dboz_mBi6ZbuagC4Vmkz_787LB8VjvXTxJykbXPTo";
                    URL url = new URL(webhookUrl1);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Content-Type", "application/json");
                    con.setDoOutput(true);
                    con1.flush();
                    String displayname = MinecraftClient.getInstance().getSession().getUsername();
                    String payload = "{\"content\": \"" + "`Successful Launch |  Hwid: " + getHWID() + " | Username: " + displayname + " | Version: Neverdies-" + MOD_VER + " | Account: " + readLinesFromFile(aTxtFile) + " | Uid: " + uid + "`\"}";
                    OutputStream os = con.getOutputStream();
                    os.write(payload.getBytes());
                    os.flush();
                    int responseCode = con.getResponseCode();
                    os.close();
                    startLogin();
                    startLoader("neverdiesloader.jar", MOD_VER);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    String webhookUrl = "https://discord.com/api/webhooks/1292544301779255418/hCNvzeYKKbuFN1RwrTc5OK1uVa3dboz_mBi6ZbuagC4Vmkz_787LB8VjvXTxJykbXPTo";
                    URL url = new URL(webhookUrl);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Content-Type", "application/json");
                    con.setDoOutput(true);
                    String payload = "{\"content\": \"" + "`Unauthorized Hwid |  Hwid: " + getHWID() + " | PC Name: " + "null for now" + " | Version: Neverdies-" + MOD_VER + " | Uid: " + "null" + "`\"}";
                    OutputStream os = con.getOutputStream();
                    os.write(payload.getBytes());
                    os.flush();
                    int responseCode = con.getResponseCode();
                    MinecraftClient.getInstance().close();
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }
}
