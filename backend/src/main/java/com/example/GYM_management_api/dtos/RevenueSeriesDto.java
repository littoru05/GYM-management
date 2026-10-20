package com.example.GYM_management_api.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Số liệu biểu đồ doanh thu và check-in theo từng ngày")
public class RevenueSeriesDto {

    @Schema(description = "Ngày thống kê (YYYY-MM-DD)", example = "2026-10-01")
    private String date;

    @Schema(description = "Doanh thu bán gói tập (UC07)", example = "15000000.0")
    private Double membershipRevenue;

    @Schema(description = "Doanh thu bán lẻ quầy (UC09)", example = "3500000.0")
    private Double retailRevenue;

    @Schema(description = "Tổng doanh thu trong ngày", example = "18500000.0")
    private Double total;

    @JsonProperty("totalRevenue")
    @Schema(description = "Tổng doanh thu trong ngày (alias cho total)", example = "18500000.0")
    public Double getTotalRevenue() {
        return total;
    }

    @Schema(description = "Số lượt check-in trong ngày từ lịch sử UC08", example = "42")
    private Long checkInCount;
}
