package com.example.GYM_management_api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Kết quả kiểm tra và áp dụng mã voucher giảm giá")
public class VoucherApplyResponseDto {

    @Schema(description = "Trạng thái hợp lệ của mã", example = "true")
    private boolean valid;

    @Schema(description = "Mã voucher", example = "SUMMER2026")
    private String code;

    @Schema(description = "Tên chương trình voucher", example = "Khuyến mãi chào hè")
    private String name;

    @Schema(description = "Hình thức giảm giá (PERCENTAGE hoặc FIXED)", example = "PERCENTAGE")
    private String discountType;

    @Schema(description = "Giá trị giảm theo cấu hình (ví dụ 10% hoặc 50000 VND)", example = "10")
    private BigDecimal discountValue;

    @Schema(description = "Tổng giá trị đơn hàng trước giảm giá (VND)", example = "500000")
    private BigDecimal orderAmount;

    @Schema(description = "Số tiền được giảm (VND)", example = "50000")
    private BigDecimal discountAmount;

    @Schema(description = "Số tiền thanh toán thực tế sau khi giảm (VND)", example = "450000")
    private BigDecimal finalAmount;

    @Schema(description = "Số lượt đã sử dụng sau khi áp dụng", example = "1")
    private Integer usedCount;

    @Schema(description = "Giới hạn số lượt dùng tối đa", example = "100")
    private Integer usageLimit;

    @Schema(description = "Thông báo kết quả", example = "Áp dụng mã giảm giá thành công.")
    private String message;
}
