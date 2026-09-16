package com.example.GYM_management_api.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ProductDto {
    private String id;
    private String sku;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    @NotBlank(message = "Danh mục sản phẩm không được để trống")
    private String category; // Category code hoặc name (DRINK, SUPPLEMENT...)

    private String categoryId;

    @NotNull(message = "Giá bán không được để trống")
    @PositiveOrZero(message = "Giá bán phải lớn hơn hoặc bằng 0")
    private BigDecimal price;

    @NotNull(message = "Giá vốn không được để trống")
    @PositiveOrZero(message = "Giá vốn phải lớn hơn hoặc bằng 0")
    private BigDecimal cost;

    @Builder.Default
    private Integer stock = 0;

    @Builder.Default
    private Integer minStock = 10;

    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, INACTIVE

    private String image;
}
