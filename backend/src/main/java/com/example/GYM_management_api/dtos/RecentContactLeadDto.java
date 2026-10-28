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
public class RecentContactLeadDto {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private String message;
    private LocalDateTime createdAt;
    private Boolean isRead;
    private String status;
}
