package com.example.GYM_management_api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Thông tin sản phẩm bán lẻ")
public class ProductDto {

    @Schema(description = "ID sản phẩm", example = "1")
    private String id;

    @Schema(description = "Mã SKU", example = "WHEY-GOLD-5LBS")
    private String sku;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Schema(description = "Tên sản phẩm", example = "Sữa bột Whey Gold Standard 5lbs")
    private String name;

    @NotBlank(message = "Danh mục sản phẩm không được để trống")
    @Schema(description = "Mã danh mục", example = "SUPPLEMENT")
    private String category; // Category code hoặc name (DRINK, SUPPLEMENT...)

    @Schema(description = "ID danh mục", example = "1")
    private String categoryId;

    @NotNull(message = "Giá bán không được để trống")
    @PositiveOrZero(message = "Giá bán phải lớn hơn hoặc bằng 0")
    @Schema(description = "Giá bán niêm yết (VND)", example = "1650000")
    private BigDecimal price;

    @NotNull(message = "Giá vốn không được để trống")
    @PositiveOrZero(message = "Giá vốn phải lớn hơn hoặc bằng 0")
    @Schema(description = "Giá vốn nhập hàng (VND)", example = "1200000")
    private BigDecimal cost;

    @Builder.Default
    @Schema(description = "Số lượng tồn kho", example = "50")
    private Integer stock = 0;

    @Builder.Default
    @Schema(description = "Mức tồn kho tối thiểu cảnh báo", example = "10")
    private Integer minStock = 10;

    @Builder.Default
    @Schema(description = "Trạng thái sản phẩm (ACTIVE, INACTIVE)", example = "ACTIVE")
    private String status = "ACTIVE"; // ACTIVE, INACTIVE

    @Schema(description = "Đường dẫn ảnh sản phẩm", example = "https://images.unsplash.com/photo-1593095948071-474c5cc2989d")
    private String image;
}
