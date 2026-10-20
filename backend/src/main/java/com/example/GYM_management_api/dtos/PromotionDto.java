package com.example.GYM_management_api.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "ID khuyến mãi (chỉ đọc, tự sinh)", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private String id;

    @NotBlank(message = "Mã voucher không được để trống")
    @Schema(description = "Mã khuyến mãi (Voucher Code)", example = "PROMO20")
    private String code;

    @NotBlank(message = "Tên chương trình khuyến mãi không được để trống")
    @Schema(description = "Tên chương trình khuyến mãi", example = "Ưu đãi 20% cho hội viên")
    private String name;

    @NotBlank(message = "Loại giảm giá không được để trống")
    @Schema(description = "Loại giảm giá (PERCENTAGE: Theo %, FIXED: Số tiền cố định)", example = "PERCENTAGE")
    private String discountType; // PERCENTAGE, FIXED

    @NotNull(message = "Giá trị giảm không được để trống")
    @Schema(description = "Giá trị giảm (Nếu PERCENTAGE: số %, nếu FIXED: số tiền VND)", example = "20")
    private BigDecimal discountValue;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    @Schema(description = "Ngày bắt đầu (YYYY-MM-DD)", example = "2026-10-01")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    @Schema(description = "Ngày kết thúc (YYYY-MM-DD)", example = "2026-12-31")
    private LocalDate endDate;

    @NotNull(message = "Giới hạn số lượt sử dụng không được để trống")
    @Min(value = 1, message = "Số lượt sử dụng tối thiểu là 1")
    @Schema(description = "Giới hạn số lượt sử dụng", example = "100")
    private Integer usageLimit;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Số lượt đã sử dụng (chỉ đọc, tự động tăng khi áp dụng)", accessMode = Schema.AccessMode.READ_ONLY, example = "0")
    private Integer usedCount;

    @Schema(description = "Giá trị đơn hàng tối thiểu để áp dụng voucher (VND)", example = "200000")
    private BigDecimal minOrderAmount;

    @Schema(description = "Mức giảm giá tối đa cho voucher giảm theo % (VND)", example = "50000")
    private BigDecimal maxDiscountAmount;

    @Schema(description = "Trạng thái kích hoạt (ACTIVE, INACTIVE)", example = "ACTIVE")
    private String status; // ACTIVE, INACTIVE

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Trạng thái hiệu lực tính toán (chỉ đọc: ACTIVE, INACTIVE, EXPIRED, UPCOMING, USED_UP)", accessMode = Schema.AccessMode.READ_ONLY, example = "ACTIVE")
    private String effectiveStatus; // ACTIVE, INACTIVE, EXPIRED, UPCOMING, USED_UP
}
