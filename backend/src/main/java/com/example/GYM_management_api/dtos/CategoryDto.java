package com.example.GYM_management_api.dtos;

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
    private String id;
    private String code;

    @NotBlank(message = "Tên danh mục không được để trống")
    private String name;

    private String description;

    private String status;
}
