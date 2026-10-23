package com.example.GYM_management_api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu đăng ký/kích hoạt gói tập cho hội viên")
public class SubscriptionRequestDto {

    public SubscriptionRequestDto(Long membershipId, LocalDate startDate, String notes) {
        this.membershipId = membershipId;
        this.startDate = startDate;
        this.notes = notes;
    }

    @NotNull(message = "Vui lòng chọn gói tập")
    @Schema(description = "ID gói tập cần đăng ký", example = "1")
    private Long membershipId;

    @NotNull(message = "Vui lòng chọn ngày bắt đầu")
    @Schema(description = "Ngày bắt đầu kích hoạt gói tập", example = "2026-09-16")
    private LocalDate startDate;

    @Schema(description = "Ghi chú (tùy chọn)", example = "Đăng ký tại quầy lễ tân")
    private String notes;

    @Schema(description = "Mã voucher (tùy chọn)", example = "WELCOME10")
    @com.fasterxml.jackson.annotation.JsonAlias({"voucher_code", "promotionCode"})
    private String voucherCode;

    @Schema(description = "Số tiền thanh toán thực tế sau khi áp dụng voucher", example = "225000")
    @com.fasterxml.jackson.annotation.JsonAlias({"paid_price", "finalPrice", "amountAfterDiscount"})
    private java.math.BigDecimal paidPrice;
}
