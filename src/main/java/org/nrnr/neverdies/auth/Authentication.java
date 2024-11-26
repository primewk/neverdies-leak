package org.nrnr.neverdies.auth;



import org.nrnr.neverdies.auth.encryt.Encryption;
import org.nrnr.neverdies.auth.hwid.HWID;
import org.nrnr.neverdies.auth.utils.NetworkUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Scanner;

public class Authentication {

    public static final File MAIN_FOLDER = new File(System.getProperty("user.dir") + File.separator + "neverdies");

    public void auth() {
        File creds = new File(MAIN_FOLDER + File.separator + "authkey");

        String password = "";
        String username = "";

        if (!NetworkUtil.isInternetAvailable()) {
            throw new RuntimeException("Generic error 0xFF (WIFI_DISABLED)");
        }

        if (creds.exists()) {
            try (Scanner scanner = new Scanner(creds)) {
                if (scanner.hasNextLine()) {
                    String encryptedData = scanner.nextLine();

                    String FOOD9RS9F9 = decrypt(encryptedData);
                    String[] credentials = FOOD9RS9F9.split(" ");
                    password = credentials[0];
                    username = credentials[1];

                    HWID.isLogin(username + " " + password);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            HWID.isHwid(HWID.getHWID());
        } else {
            // implement ur logging in right here, checking n everything idk how u wanna do it, and if u wanna do it with jpanels. - vasler

            String combo;

            String[] credentials = {password, username};

            StringBuilder encryptedCombo = new StringBuilder();

            for (String cred : credentials) {
                byte[] encrypted = cred.getBytes(StandardCharsets.UTF_8);
                byte[] xor = Encryption.xor(encrypted, HWID.k);

                combo = bytesToHex(Base64.getEncoder().encode(xor));

                encryptedCombo.append(combo).append(" ");
            }

            try {
                creds.createNewFile();
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(creds))) {
                    writer.write(encryptedCombo.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String decrypt(String encrypt) {
        byte[] base64 = Base64.getDecoder().decode(encrypt);

        byte[] decrypted = Encryption.xor(hexToBytes(new String(base64, StandardCharsets.UTF_8)), HWID.k);

        return new String(decrypted, StandardCharsets.UTF_8);
    }

    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
