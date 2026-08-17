package com.sunrisedental.clinic.util;

import java.util.Arrays;
import java.util.List;

/**
 * Shared clinic constants used across servlets and views.
 */
public final class ClinicConstants {

    public static final List<String> DENTISTS = Arrays.asList(
            "Dr. Nimal Perera",
            "Dr. Sanduni Fernando",
            "Dr. Ruwan Silva",
            "Dr. Amaya Jayawardena"
    );

    private ClinicConstants() {
    }
}
