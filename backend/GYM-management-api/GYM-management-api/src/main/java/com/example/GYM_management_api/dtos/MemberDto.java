package com.example.GYM_management_api.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDto {
    private String id;
    private String code;

    @NotBlank(message = "Tên hội viên không được để trống")
    private String name;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;

    private String email;

    private LocalDate dob;

    private LocalDate joinDate;

    private String membershipId;

    private String membershipName;

    private LocalDate expiryDate;

    private String status; // ACTIVE, EXPIRING_SOON, EXPIRED

    private String avatar;

    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(java.time.LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setCreatedAt(String val) {
        this.createdAt = parseIsoDateTime(val);
    }

    public void setUpdatedAt(String val) {
        this.updatedAt = parseIsoDateTime(val);
    }

    private static java.time.LocalDateTime parseIsoDateTime(String val) {
        if (val == null || val.trim().isEmpty())
            return null;
        try {
            if (val.endsWith("Z") || val.contains("+")) {
                return java.time.OffsetDateTime.parse(val).atZoneSameInstant(java.time.ZoneId.systemDefault())
                        .toLocalDateTime();
            }
            return java.time.LocalDateTime.parse(val);
        } catch (Exception e) {
            return java.time.LocalDateTime.now();
        }
    }
}
