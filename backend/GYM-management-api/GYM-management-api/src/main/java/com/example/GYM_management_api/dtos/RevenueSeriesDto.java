package com.example.GYM_management_api.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueSeriesDto {
    private String date;
    private Double membershipRevenue;
    private Double retailRevenue;
    private Double total;
}
