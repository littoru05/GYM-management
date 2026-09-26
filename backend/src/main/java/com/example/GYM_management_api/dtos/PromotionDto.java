package com.example.GYM_management_api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin mã giảm giá / khuyến mãi")
public class PromotionDto {

    @Schema(description = "ID khuyến mãi", example = "1")
    private String id;

    @NotBlank(message = "Mã voucher không được để trống")
    @Schema(description = "Mã khuyến mãi (Voucher Code)", example = "SUMMER2026")
    private String code;

    @NotBlank(message = "Tên chương trình khuyến mãi không được để trống")
    @Schema(description = "Tên chương trình khuyến mãi", example = "Khuyến mãi chào hè")
    private String name;

    @NotBlank(message = "Loại giảm giá không được để trống")
    @Schema(description = "Loại giảm giá (PERCENTAGE: Theo %, FIXED: Số tiền cố định)", example = "PERCENTAGE")
    private String discountType; // PERCENTAGE, FIXED

    @NotNull(message = "Giá trị giảm không được để trống")
    @Schema(description = "Giá trị giảm", example = "10")
    private BigDecimal discountValue;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    @Schema(description = "Ngày bắt đầu", example = "2026-06-01")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    @Schema(description = "Ngày kết thúc", example = "2026-08-31")
    private LocalDate endDate;

    @NotNull(message = "Giới hạn số lượt sử dụng không được để trống")
    @Min(value = 1, message = "Số lượt sử dụng tối thiểu là 1")
    @Schema(description = "Giới hạn số lượt sử dụng", example = "100")
    private Integer usageLimit;

    @Schema(description = "Số lượt đã sử dụng", example = "15")
    private Integer usedCount;

    @Schema(description = "Trạng thái kích hoạt (ACTIVE, INACTIVE)", example = "ACTIVE")
    private String status; // ACTIVE, INACTIVE

    @Schema(description = "Trạng thái hiệu lực tính toán (ACTIVE, INACTIVE, EXPIRED, UPCOMING, USED_UP)", example = "ACTIVE")
    private String effectiveStatus; // ACTIVE, INACTIVE, EXPIRED, UPCOMING, USED_UP
}
