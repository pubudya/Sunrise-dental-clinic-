package com.sunrisedental.clinic.util;

/**
 * Clinic account roles used for login redirects and admin-only access.
 */
public final class RoleConstants {

    public static final String ADMIN = "ADMIN";
    public static final String STAFF = "STAFF";

    private RoleConstants() {
    }

    public static boolean isAdmin(String role) {
        return ADMIN.equalsIgnoreCase(role);
    }
}
