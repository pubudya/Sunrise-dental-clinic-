package com.sunrisedental.clinic.util;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtil {

    private PasswordUtil() {
    }

    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    public static boolean matches(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        if (!hashedPassword.startsWith("$2")) {
            return plainPassword.equals(hashedPassword);
        }
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
