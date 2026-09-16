package com.example.GYM_management_api.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipStatisticsDto {
    private long active;
    private long expiringSoon;
    private long expired;
    private long newMembers;
    private long total;
    private long todayCheckIns;
}
