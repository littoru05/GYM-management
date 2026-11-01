package com.example.GYM_management_api.entities.enums;

public enum LeadStatus {
    NEW,         // Mới tạo / Chưa tư vấn
    CONTACTED,   // Đã liên hệ
    PENDING,     // Chờ tư vấn
    CONVERTED,   // Đã đăng ký gói tập
    CANCELLED    // Hủy / Không có nhu cầu
}
