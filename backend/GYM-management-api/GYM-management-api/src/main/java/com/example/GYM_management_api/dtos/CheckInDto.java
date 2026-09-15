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
public class CheckInDto {
    private String id;
    private String code;
    private String memberId;
    private String memberName;
    private String avatar;
    private String membershipName;
    private LocalDateTime time;
    private LocalDateTime checkOutTime;
    private String status; // SUCCESS, DENIED
    private String reason;
}
