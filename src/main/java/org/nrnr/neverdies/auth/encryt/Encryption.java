package org.nrnr.neverdies.auth.encryt;

import javax.crypto.Cipher;

public class Encryption {

    public static byte[] xor(byte[] data, byte[] k) {
        byte[] r = new byte[data.length];
        for (int i = 0; i < data.length; i++) r[i] = (byte) (data[i] ^ k[i % k.length]);
        return r;
    }
}
