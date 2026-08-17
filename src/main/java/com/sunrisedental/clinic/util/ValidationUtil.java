package com.sunrisedental.clinic.util;

public final class ValidationUtil {

    public static final String MOBILE_PATTERN = "^[0-9]{10}$";

    private ValidationUtil() {
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static String requireNonBlank(String value, String fieldName) {
        if (isBlank(value)) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return value.trim();
    }

    public static String requireMobile(String value, String fieldName) {
        String trimmed = requireNonBlank(value, fieldName);
        if (!trimmed.matches(MOBILE_PATTERN)) {
            throw new IllegalArgumentException(fieldName + " must be exactly 10 digits.");
        }
        return trimmed;
    }

    public static boolean hasAnySearchCriteria(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
                return true;
            }
        }
        return false;
    }
}
