package com.example.GYM_management_api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dữ liệu phản hồi báo cáo doanh thu biểu đồ")
public class RevenueReportResponseDto {

    @Schema(description = "Thông số tổng quan doanh thu và check-in")
    private RevenueSummaryDto summary;

    @Schema(description = "Danh sách điểm dữ liệu theo ngày phục vụ vẽ biểu đồ (Timeline series)")
    private List<RevenueSeriesDto> chartData;
}
