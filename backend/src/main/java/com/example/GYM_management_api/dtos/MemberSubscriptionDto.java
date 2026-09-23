package com.example.GYM_management_api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin lịch sử đăng ký gói tập của hội viên")
public class MemberSubscriptionDto {

    @Schema(description = "ID đăng ký", example = "1")
    private Long id;

    @Schema(description = "Mã đăng ký", example = "SUB-0001")
    private String code;

    @Schema(description = "ID gói tập đã đăng ký", example = "1")
    private Long membershipId;

    @Schema(description = "Tên gói tập", example = "Gói 1 tháng")
    private String membershipName;

    @Schema(description = "Ngày bắt đầu", example = "2026-09-16")
    private LocalDate startDate;

    @Schema(description = "Ngày kết thúc / hết hạn", example = "2026-10-16")
    private LocalDate endDate;

    @Schema(description = "Số tiền thực tế đã thanh toán", example = "270000.00")
    private BigDecimal paidPrice;

    @Schema(description = "Trạng thái gói tập (ACTIVE, EXPIRED, CANCELLED, PENDING)", example = "ACTIVE")
    private String status;

    @Schema(description = "Ghi chú", example = "Đăng ký tại quầy lễ tân")
    private String notes;

    @Schema(description = "Thời điểm đăng ký")
    private LocalDateTime createdAt;
}
