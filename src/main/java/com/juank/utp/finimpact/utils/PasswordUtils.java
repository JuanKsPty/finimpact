package com.juank.utp.finimpact.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class PasswordUtils {

    private static final String HASH_ALGORITHM = "SHA-256";

    /**
     * Hashea una contraseña directamente (sin salt)
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al hashear contraseña", e);
        }
    }

    /**
     * Verifica una contraseña contra su hash
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        String hashToVerify = hashPassword(password);
        return hashToVerify.equals(hashedPassword);
    }
}
