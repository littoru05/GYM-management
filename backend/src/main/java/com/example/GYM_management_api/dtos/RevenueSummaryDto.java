package com.example.GYM_management_api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Báo cáo tổng quan doanh thu")
public class RevenueSummaryDto {

    @Schema(description = "Doanh thu hôm nay (VND)", example = "12500000.0")
    private Double todayRevenue;

    @Schema(description = "Doanh thu tháng này (VND)", example = "185000000.0")
    private Double monthRevenue;

    @Schema(description = "Số lượng giao dịch hôm nay", example = "15")
    private Integer todayTransactions;

    @Schema(description = "Doanh thu từ gói tập (VND)", example = "150000000.0")
    private Double membershipRevenue;

    @Schema(description = "Doanh thu bán lẻ sản phẩm (VND)", example = "35000000.0")
    private Double retailRevenue;
}
