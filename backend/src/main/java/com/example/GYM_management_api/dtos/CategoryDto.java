package com.example.GYM_management_api.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "ID danh mục (tự động tăng, chỉ đọc)", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private String id;

    @Schema(description = "Mã danh mục", example = "CAT_WHEY")
    private String code;

    @NotBlank(message = "Tên danh mục không được để trống")
    @Schema(description = "Tên danh mục", example = "Thực phẩm bổ sung")
    private String name;

    @Schema(description = "Mô tả danh mục", example = "Các dòng đạm whey và dinh dưỡng thể hình")
    private String description;

    @Schema(description = "Trạng thái danh mục (ACTIVE, INACTIVE)", example = "ACTIVE")
    private String status;
}
