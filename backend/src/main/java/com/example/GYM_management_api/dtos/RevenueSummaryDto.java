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

    @Schema(description = "Tổng doanh thu trong khoảng thời gian đã chọn (VND)", example = "185000000.0")
    private Double totalRevenue;

    @Schema(description = "Doanh thu từ gói tập (UC07) (VND)", example = "150000000.0")
    private Double membershipRevenue;

    @Schema(description = "Doanh thu bán lẻ sản phẩm (UC09) (VND)", example = "35000000.0")
    private Double retailRevenue;

    @Schema(description = "Tổng số lượt check-in trong khoảng thời gian (UC08)", example = "350")
    private Long totalCheckIns;

    @Schema(description = "Doanh thu hôm nay (VND)", example = "12500000.0")
    private Double todayRevenue;

    @Schema(description = "Doanh thu tháng này (VND)", example = "185000000.0")
    private Double monthRevenue;

    @Schema(description = "Số lượng giao dịch hôm nay", example = "15")
    private Integer todayTransactions;
}
