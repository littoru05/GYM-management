package com.example.GYM_management_api.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactLeadDto {
    private String id;
    private String name;
    private String phone;
    private String email;
    private String message;
    private String status; // PENDING, CONTACTED, CONVERTED, CANCELLED
    private String notes;
    private String assignedStaffName;
    private LocalDateTime createdAt;
}
