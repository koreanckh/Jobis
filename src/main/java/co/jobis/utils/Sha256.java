package co.jobis.utils;

import java.security.MessageDigest;

public class Sha256 {

    public static String encrypt(String inputStr) {

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(inputStr.getBytes());

            byte[] byteData = md.digest();
            StringBuilder sb = new StringBuilder();

            for (byte datum : byteData) {
                sb.append(Integer.toString((datum & 0xff) + 0x100, 16).substring(1));
            }

            StringBuilder hexString = new StringBuilder();

            for (byte byteDatum : byteData) {
                String hex = Integer.toHexString(0xff & byteDatum);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
}
