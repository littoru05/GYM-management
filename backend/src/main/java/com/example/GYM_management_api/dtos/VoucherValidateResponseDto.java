package com.example.GYM_management_api.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Kết quả kiểm tra tính hợp lệ của mã voucher")
public class VoucherValidateResponseDto {

    @Schema(description = "Trạng thái hợp lệ của voucher", example = "true")
    private boolean valid;

    @Schema(description = "Mã voucher", example = "WELCOME10")
    private String code;

    @Schema(description = "Tên chương trình khuyến mãi", example = "Ưu đãi hội viên mới")
    private String name;

    @Schema(description = "Hình thức giảm giá (PERCENT / PERCENTAGE / FIXED / FREE)", example = "PERCENT")
    private String discountType;

    @Schema(description = "Giá trị giảm theo cấu hình (ví dụ 10% hoặc 50000 VND)", example = "10")
    private BigDecimal discountValue;

    @Schema(description = "Giá trị đơn hàng tối thiểu để áp dụng voucher", example = "0")
    @JsonAlias({"minOrderAmount", "min_order_value", "min_order_amount"})
    private BigDecimal minOrderValue;

    @Schema(description = "Mức giảm tối đa (VND)", example = "50000")
    @JsonAlias({"maxDiscountAmount", "max_discount", "max_discount_amount"})
    private BigDecimal maxDiscount;

    @Schema(description = "Có áp dụng cho tất cả gói tập không", example = "true")
    @Builder.Default
    private Boolean applicableToAll = true;

    @Schema(description = "Danh sách ID gói tập được áp dụng nếu không phải tất cả", example = "1,2,3")
    private String applicableMembershipIds;

    @Schema(description = "Ngày hết hạn của voucher", example = "2026-12-31")
    @JsonAlias({"endDate", "end_date", "expirationDate"})
    private LocalDate expiredAt;

    @Schema(description = "Trạng thái voucher (ACTIVE, INACTIVE, EXPIRED)", example = "ACTIVE")
    private String status;

    @Schema(description = "Giá trị đơn hàng trước khi giảm giá (VND)", example = "250000")
    private BigDecimal orderAmount;

    @Schema(description = "Số tiền được giảm giá (VND)", example = "25000")
    private BigDecimal discountAmount;

    @Schema(description = "Số tiền sau khi giảm giá (VND)", example = "225000")
    @JsonAlias({"amount_after_discount", "finalAmount", "final_amount"})
    private BigDecimal amountAfterDiscount;

    @Schema(description = "Giá thanh toán cuối cùng (VND)", example = "225000")
    @JsonAlias({"final_price", "paidPrice"})
    private BigDecimal finalPrice;

    @Schema(description = "Thông báo kết quả kiểm tra", example = "Áp dụng mã giảm giá thành công.")
    private String message;

    @JsonProperty("finalAmount")
    public BigDecimal getFinalAmount() {
        return finalPrice != null ? finalPrice : amountAfterDiscount;
    }
}
