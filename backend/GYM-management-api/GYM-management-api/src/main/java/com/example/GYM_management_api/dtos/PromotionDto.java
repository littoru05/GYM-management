package com.example.GYM_management_api.dtos;

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
public class PromotionDto {
    private String id;

    @NotBlank(message = "Mã voucher không được để trống")
    private String code;

    @NotBlank(message = "Tên chương trình khuyến mãi không được để trống")
    private String name;

    @NotBlank(message = "Loại giảm giá không được để trống")
    private String discountType; // PERCENTAGE, FIXED

    @NotNull(message = "Giá trị giảm không được để trống")
    private BigDecimal discountValue;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate endDate;

    @NotNull(message = "Giới hạn số lượt sử dụng không được để trống")
    @Min(value = 1, message = "Số lượt sử dụng tối thiểu là 1")
    private Integer usageLimit;

    private Integer usedCount;

    private String status; // ACTIVE, INACTIVE

    private String effectiveStatus; // ACTIVE, INACTIVE, EXPIRED, UPCOMING, USED_UP
}
