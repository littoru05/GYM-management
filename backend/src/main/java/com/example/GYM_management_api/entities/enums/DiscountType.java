package com.example.GYM_management_api.entities.enums;

public enum DiscountType {
    PERCENTAGE,
    PERCENT,
    FIXED,
    FREE;

    public static DiscountType fromString(String val) {
        if (val == null || val.isBlank()) {
            return PERCENTAGE;
        }
        String clean = val.trim().toUpperCase();
        if ("PERCENT".equals(clean)) {
            return PERCENTAGE;
        }
        for (DiscountType type : values()) {
            if (type.name().equalsIgnoreCase(clean)) {
                return type;
            }
        }
        return PERCENTAGE;
    }
}

