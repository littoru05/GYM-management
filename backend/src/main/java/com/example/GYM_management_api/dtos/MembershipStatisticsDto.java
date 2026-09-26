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
@Schema(description = "Thống kê tình trạng hội viên")
public class MembershipStatisticsDto {

    @Schema(description = "Số lượng hội viên đang hoạt động", example = "120")
    private long active;

    @Schema(description = "Số lượng hội viên sắp hết hạn", example = "8")
    private long expiringSoon;

    @Schema(description = "Số lượng hội viên đã hết hạn", example = "15")
    private long expired;

    @Schema(description = "Số lượng hội viên mới trong tháng", example = "25")
    private long newMembers;

    @Schema(description = "Tổng số hội viên", example = "160")
    private long total;

    @Schema(description = "Số lượt check-in hôm nay", example = "42")
    private long todayCheckIns;
}
