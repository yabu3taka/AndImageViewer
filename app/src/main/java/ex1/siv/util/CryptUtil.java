package ex1.siv.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CryptUtil {
    private CryptUtil() {
    }

    public static String md5(String s) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte[] messageDigest = digest.digest();

            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                hexString.append(Integer.toHexString(0xFF & aMessageDigest));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException ignored) {
        }
        return "";
    }
}
