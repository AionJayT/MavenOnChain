package org.aion.maven.blockchain;


/**
 * This implementation is partially copied from org.aion.avm.core.util.Helpers.
 */
public class Codecs {
    private static final char[] hexArray = "0123456789abcdef".toCharArray();

    public static String bytesToHexString(byte[] bytes) {
        if (bytes.length == 0){
            return "void";
        }

        int length = bytes.length;

        char[] hexChars = new char[length * 2];
        for (int i = 0; i < length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = hexArray[v >>> 4];
            hexChars[i * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexStringToBytes(String s) {
        if (s.startsWith("0x")) {
            s = s.substring(2);
        }

        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static long hexStringToLong(String s) {
        return s.startsWith("0x")
                ? Long.parseLong(s.substring(2), 16)
                : Long.parseLong(s, 16);
    }

    public static int hexStringToInteger(String s) {
        return s.startsWith("0x")
                ? Integer.parseInt(s.substring(2), 16)
                : Integer.parseInt(s, 16);
    }
}
