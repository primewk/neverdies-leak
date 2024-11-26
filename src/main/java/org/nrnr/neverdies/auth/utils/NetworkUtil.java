package org.nrnr.neverdies.auth.utils;

import java.net.InetAddress;

public class NetworkUtil {

    public static boolean isInternetAvailable() {
        try {
            InetAddress address = InetAddress.getByName("www.google.com");
            return address.isReachable(1000);
        } catch (Exception e) {
            return false;
        }
    }
}
