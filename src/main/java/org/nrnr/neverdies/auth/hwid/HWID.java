package org.nrnr.neverdies.auth.hwid;

import org.apache.commons.codec.digest.DigestUtils;
import org.nrnr.neverdies.auth.encryt.Encryption;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class HWID {
    public static byte[] k = "GAAANG".getBytes();

    public static String getHWID() {
        String hwid = System.getenv("os") +
                System.getProperty("os.name") +
                System.getProperty("os.arch") +
                System.getProperty("os.version") +
                System.getProperty("user.language") +
                System.getenv("SystemRoot") +
                System.getenv("HOMEDRIVE") +
                System.getenv("PROCESSOR_LEVEL") +
                System.getenv("PROCESSOR_REVISION") +
                System.getenv("PROCESSOR_IDENTIFIER") +
                System.getenv("PROCESSOR_ARCHITECTURE") +
                System.getenv("PROCESSOR_ARCHITEW6432") +
                System.getenv("NUMBER_OF_PROCESSORS");

        byte[] bytes = hwid.getBytes(StandardCharsets.UTF_8);

        byte[] xor = Encryption.xor(bytes, k);

        return DigestUtils.sha3_256Hex(DigestUtils.md2Hex(DigestUtils.sha512Hex(DigestUtils.sha512Hex(
                                        System.getenv("os") +
                                                System.getProperty("os.name") +
                                                System.getProperty("os.arch") +
                                                System.getProperty("os.version") +
                                                System.getProperty("user.language") +
                                                System.getenv("SystemRoot") +
                                                System.getenv("HOMEDRIVE") +
                                                System.getenv("PROCESSOR_LEVEL") +
                                                System.getenv("PROCESSOR_REVISION") +
                                                System.getenv("PROCESSOR_IDENTIFIER") +
                                                System.getenv("PROCESSOR_ARCHITECTURE") +
                                                System.getenv("PROCESSOR_ARCHITEW6432") +
                                                System.getenv("NUMBER_OF_PROCESSORS")))));
    }

    public static boolean isHwid(String hwid) {
        try {
            URI uri = new URI("LINK HERE");

            HttpURLConnection connection = (HttpURLConnection) new URL(uri.toASCIIString()).openConnection();
            connection.setRequestMethod("GET");

            try (InputStream inputStream = connection.getInputStream(); Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8)) {
                String s = scanner.useDelimiter("\\A").next();
                return s.contains(hwid);
            }

        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isLogin(String shit) {
        try {
            URI uri = new URI("LINK HERE");

            HttpURLConnection connection = (HttpURLConnection) new URL(uri.toASCIIString()).openConnection();
            connection.setRequestMethod("GET");

            try (InputStream inputStream = connection.getInputStream(); Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8)) {
                String s = scanner.useDelimiter("\\A").next();
                return s.contains(shit);
            }

        } catch (Exception e) {
            return false;
        }
    }
}
