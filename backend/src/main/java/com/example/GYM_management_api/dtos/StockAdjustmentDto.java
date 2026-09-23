package com.example.GYM_management_api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu điều chỉnh tồn kho sản phẩm")
public class StockAdjustmentDto {

    @NotNull(message = "Số lượng thay đổi không được để trống")
    @Schema(description = "Số lượng thay đổi (dương: nhập kho, âm: xuất/hỏng)", example = "10")
    private Integer quantityChange;

    @Schema(description = "Lý do điều chỉnh", example = "Nhập thêm hàng từ nhà cung cấp")
    private String reason;
}
