package com.example.GYM_management_api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {
    private String id;
    private String code;
    private LocalDate date;

    private LocalDateTime transactionTime;

    public void setTransactionTime(LocalDateTime transactionTime) {
        this.transactionTime = transactionTime;
    }

    public void setTransactionTime(String val) {
        if (val == null || val.trim().isEmpty()) {
            this.transactionTime = null;
            return;
        }
        try {
            if (val.endsWith("Z") || val.contains("+")) {
                this.transactionTime = java.time.OffsetDateTime.parse(val)
                        .atZoneSameInstant(java.time.ZoneId.systemDefault()).toLocalDateTime();
            } else {
                this.transactionTime = java.time.LocalDateTime.parse(val);
            }
        } catch (Exception e) {
            this.transactionTime = java.time.LocalDateTime.now();
        }
    }

    @NotBlank(message = "Loại giao dịch không được để trống")
    private String type; // MEMBERSHIP, RETAIL

    private String memberId;
    private String memberName;
    private String membershipId;
    private String membershipName;
    private String trainerName;
    private String staffId;
    private String staffName;
    private String promotionCode;

    private String description;

    @NotNull(message = "Số tiền giao dịch không được để trống")
    @Schema(description = "Số tiền thanh toán cuối cùng (VND)", example = "5000000")
    private BigDecimal amount;

    @Schema(description = "Tổng tiền trước giảm giá (VND)", example = "5000000")
    private BigDecimal subtotal;

    @Schema(description = "Số tiền giảm giá (VND)", example = "0")
    private BigDecimal discountAmount;

    @NotBlank(message = "Phương thức thanh toán không được để trống")
    @Schema(description = "Phương thức thanh toán (CASH, BANK_TRANSFER, CARD)", example = "CASH")
    private String paymentMethod; // CASH, BANK_TRANSFER, CARD

    @Builder.Default
    private List<TransactionDetailDto> items = new ArrayList<>();
}
