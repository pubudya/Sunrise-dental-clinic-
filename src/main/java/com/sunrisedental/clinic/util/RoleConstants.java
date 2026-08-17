package com.sunrisedental.clinic.util;

public final class RoleConstants {

    public static final String ADMIN = "ADMIN";
    public static final String STAFF = "STAFF";

    private RoleConstants() {
    }

    public static boolean isAdmin(String role) {
        return ADMIN.equalsIgnoreCase(role);
    }
}
