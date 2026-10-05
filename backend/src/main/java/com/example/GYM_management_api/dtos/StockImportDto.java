package com.example.GYM_management_api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
@Schema(description = "Yêu cầu nhập hàng tăng tồn kho sản phẩm")
public class StockImportDto {

    @NotNull(message = "Số lượng nhập không được để trống")
    @Positive(message = "Số lượng nhập kho phải lớn hơn 0")
    @Schema(description = "Số lượng nhập thêm vào kho (> 0)", example = "20")
    private Integer quantity;

    @PositiveOrZero(message = "Đơn giá vốn nhập mới không được là số âm")
    @Schema(description = "Đơn giá vốn nhập mới (VND, tùy chọn cập nhật)", example = "1200000")
    private BigDecimal cost;

    @Schema(description = "Ghi chú nhập kho (nhà cung cấp, mã phiếu...)", example = "Nhập lô hàng mới tháng 10")
    private String note;
}
