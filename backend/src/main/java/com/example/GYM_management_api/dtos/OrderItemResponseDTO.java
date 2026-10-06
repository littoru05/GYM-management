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
@Schema(description = "Chi tiết món hàng trong đơn bán lẻ POS")
public class OrderItemResponseDTO {

    @Schema(description = "ID chi tiết đơn hàng", example = "1")
    private Long id;

    @Schema(description = "ID sản phẩm", example = "1")
    private Long productId;

    @Schema(description = "Mã SKU sản phẩm", example = "DRK001")
    private String productSku;

    @Schema(description = "Tên sản phẩm", example = "Nước khoáng Lavie 500ml")
    private String productName;

    @Schema(description = "Số lượng mua", example = "2")
    private Integer quantity;

    @Schema(description = "Đơn giá (VND)", example = "15000.00")
    private BigDecimal unitPrice;

    @Schema(description = "Giá vốn (VND)", example = "8000.00")
    private BigDecimal unitCost;

    @Schema(description = "Thành tiền (VND) (lineTotal = unitPrice * quantity)", example = "30000.00")
    private BigDecimal totalPrice;
}
