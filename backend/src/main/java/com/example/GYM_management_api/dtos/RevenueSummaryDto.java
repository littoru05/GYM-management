package com.example.GYM_management_api.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueSummaryDto {
    private Double todayRevenue;
    private Double monthRevenue;
    private Integer todayTransactions;
    private Double membershipRevenue;
    private Double retailRevenue;
}
