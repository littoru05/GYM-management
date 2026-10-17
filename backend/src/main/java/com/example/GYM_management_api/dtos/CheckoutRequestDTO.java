package com.example.GYM_management_api.dtos;

import com.example.GYM_management_api.entities.enums.PaymentMethod;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu thanh toán đơn hàng Mini POS (UC09)")
public class CheckoutRequestDTO {

    @NotEmpty(message = "Danh sách sản phẩm items không được để trống")
    @Schema(description = "Danh sách các mặt hàng thanh toán")
    @Builder.Default
    private List<@Valid OrderItemRequestDTO> items = new ArrayList<>();

    @NotNull(message = "Phương thức thanh toán (paymentMethod) không được để trống")
    @Schema(description = "Phương thức thanh toán (CASH, BANK_TRANSFER, CARD)", example = "CASH")
    @JsonProperty("paymentMethod")
    @JsonAlias({ "payment_method", "method" })
    private PaymentMethod paymentMethod;

    @Schema(description = "ID hội viên (tùy chọn nếu khách là hội viên phòng gym)", example = "1")
    @JsonProperty("memberId")
    @JsonAlias({ "member_id", "customerId", "customer_id" })
    private Long memberId;

    @Min(value = 0, message = "Số tiền giảm giá (discountAmount) không được là số âm")
    @PositiveOrZero(message = "Số tiền giảm giá (discountAmount) không được là số âm")
    @Schema(description = "Số tiền giảm giá (VND)", example = "0")
    @JsonProperty("discountAmount")
    @JsonAlias({ "discount", "discount_amount" })
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Schema(description = "Ghi chú đơn hàng", example = "Khách thanh toán tại quầy POS")
    @JsonProperty("notes")
    @JsonAlias({ "note", "description", "remark" })
    private String notes;

    @com.fasterxml.jackson.annotation.JsonIgnore
    public String getDescription() {
        return this.notes;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public void setDescription(String description) {
        this.notes = description;
    }

    public void setPaymentMethod(Object method) {
        if (method == null) {
            this.paymentMethod = null;
            return;
        }
        if (method instanceof PaymentMethod pm) {
            this.paymentMethod = pm;
        } else if (method instanceof String str) {
            try {
                this.paymentMethod = PaymentMethod.valueOf(str.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                this.paymentMethod = PaymentMethod.CASH;
            }
        }
    }
}
