package com.example.GYM_management_api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu kiểm tra và áp dụng mã voucher giảm giá")
public class VoucherApplyRequestDto {

    @NotBlank(message = "Mã voucher không được để trống")
    @Schema(description = "Mã giảm giá", example = "PROMO20")
    private String code;

    @NotNull(message = "Giá trị đơn hàng không được để trống")
    @PositiveOrZero(message = "Giá trị đơn hàng không được là số âm")
    @Schema(description = "Tổng giá trị đơn hàng trước khi giảm giá (VND)", example = "500000")
    private BigDecimal orderAmount;

    @Schema(description = "ID hội viên (tùy chọn)", example = "1")
    private Long memberId;
}
