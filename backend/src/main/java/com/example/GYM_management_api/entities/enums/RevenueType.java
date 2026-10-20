package com.example.GYM_management_api.entities.enums;

import com.example.GYM_management_api.exceptions.BadRequestException;

public enum RevenueType {
    ALL,
    MEMBERSHIP,
    RETAIL;

    public static RevenueType fromString(String value) {
        if (value == null || value.isBlank()) {
            return ALL;
        }
        try {
            return RevenueType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Loại doanh thu không hợp lệ: '" + value + "'. Các giá trị hợp lệ: ALL, MEMBERSHIP, RETAIL");
        }
    }
}
