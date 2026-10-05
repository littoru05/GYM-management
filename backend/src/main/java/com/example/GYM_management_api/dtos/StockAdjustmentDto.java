package com.example.GYM_management_api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Yêu cầu nhập kho hoặc điều chỉnh tồn kho sản phẩm")
public class StockAdjustmentDto {

    @Schema(description = "Số lượng nhập thêm vào kho (khi gọi API nhập hàng)", example = "20")
    private Integer quantity;

    @Schema(description = "Số lượng thay đổi (dương: nhập thêm, âm: hao hụt/xuất bớt)", example = "10")
    private Integer quantityChange;

    @PositiveOrZero(message = "Số lượng tồn kho thực tế không được là số âm")
    @Schema(description = "Số lượng tồn kho thực tế sau kiểm kê (>= 0). Gán trực tiếp giá trị này làm tồn kho mới", example = "45")
    private Integer actualStock;

    @PositiveOrZero(message = "Đơn giá vốn nhập mới không được là số âm")
    @Schema(description = "Đơn giá vốn nhập mới (tùy chọn cập nhật khi nhập hàng)", example = "1200000")
    private BigDecimal cost;

    @Schema(description = "Lý do điều chỉnh hoặc kiểm kê", example = "Kiểm kê định kỳ tháng 10")
    private String reason;

    @Schema(description = "Ghi chú nhập kho (nhà cung cấp, mã phiếu...)", example = "Nhập lô hàng tháng 10")
    private String note;
}
