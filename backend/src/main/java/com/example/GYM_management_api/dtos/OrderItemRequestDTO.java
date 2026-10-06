package com.example.GYM_management_api.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu chi tiết từng mặt hàng trong giỏ thanh toán POS")
public class OrderItemRequestDTO {

    @NotNull(message = "ID sản phẩm (productId) không được để trống")
    @Schema(description = "ID sản phẩm cần mua", example = "1")
    @JsonProperty("productId")
    @JsonAlias({"product_id", "id"})
    private Long productId;

    @NotNull(message = "Số lượng mua (quantity) không được để trống")
    @Min(value = 1, message = "Số lượng mua phải là số nguyên dương lớn hơn 0 (@Min(1))")
    @Schema(description = "Số lượng mua (tối thiểu 1)", example = "2")
    @JsonProperty("quantity")
    @JsonAlias({"qty", "amount"})
    private Integer quantity;
}
