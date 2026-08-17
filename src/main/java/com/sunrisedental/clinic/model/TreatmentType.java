package com.sunrisedental.clinic.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Predefined treatment types and standard charges in LKR.
 * Assumption: Clinic management maintains a fixed price list for common treatments.
 */
public final class TreatmentType {

    public static final double CONSULTATION_FEE = 1500.00;

    private static final Map<String, Double> TREATMENTS = new LinkedHashMap<>();

    static {
        TREATMENTS.put("General Checkup", 2500.00);
        TREATMENTS.put("Teeth Cleaning", 3500.00);
        TREATMENTS.put("Tooth Filling", 4500.00);
        TREATMENTS.put("Root Canal", 12000.00);
        TREATMENTS.put("Tooth Extraction", 6000.00);
        TREATMENTS.put("Dental Crown", 18000.00);
        TREATMENTS.put("Teeth Whitening", 8000.00);
        TREATMENTS.put("Orthodontic Consultation", 5000.00);
    }

    private TreatmentType() {
    }

    public static Set<String> getAllTypes() {
        return TREATMENTS.keySet();
    }

    public static double getTreatmentCost(String treatmentType) {
        return TREATMENTS.getOrDefault(treatmentType, 0.0);
    }

    public static boolean isValid(String treatmentType) {
        return TREATMENTS.containsKey(treatmentType);
    }
}
