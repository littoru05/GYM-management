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
@Schema(description = "Chi tiết mục trong giao dịch")
public class TransactionDetailDto {

    @Schema(description = "ID chi tiết giao dịch", example = "1")
    private String id;

    @Schema(description = "ID sản phẩm (nếu là mua sản phẩm)", example = "1")
    private String productId;

    @Schema(description = "ID gói tập (nếu là mua gói)", example = "1")
    private String membershipId;

    @Schema(description = "ID huấn luyện viên (nếu thuê PT)", example = "1")
    private String trainerId;

    @Schema(description = "Loại mục (MEMBERSHIP, PRODUCT, PT)", example = "MEMBERSHIP")
    private String itemType;

    @Schema(description = "Tên mục hiển thị", example = "Gói Gold 12 Tháng")
    private String itemName;

    @Schema(description = "Số lượng", example = "1")
    private Integer quantity;

    @Schema(description = "Đơn giá (VND)", example = "5000000")
    private BigDecimal unitPrice;

    @Schema(description = "Giá vốn (VND)", example = "3000000")
    private BigDecimal unitCost;

    @Schema(description = "Tổng tiền thành phần (VND)", example = "5000000")
    private BigDecimal totalPrice;
}
