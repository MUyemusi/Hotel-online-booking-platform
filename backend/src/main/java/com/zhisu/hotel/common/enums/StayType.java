package com.zhisu.hotel.common.enums;

public enum StayType {
    STANDARD,
    EARLY_MORNING;

    public static StayType from(String value) {
        if (value == null || value.isBlank()) return STANDARD;
        try {
            return StayType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
