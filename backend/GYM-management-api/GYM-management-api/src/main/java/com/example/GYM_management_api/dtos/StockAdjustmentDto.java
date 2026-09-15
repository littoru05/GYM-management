package com.example.GYM_management_api.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustmentDto {
    @NotNull(message = "Số lượng thay đổi không được để trống")
    private Integer quantityChange;
    private String reason;
}
