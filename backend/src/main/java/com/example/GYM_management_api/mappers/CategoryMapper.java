package com.example.GYM_management_api.mappers;

import com.example.GYM_management_api.dtos.CategoryDto;
import com.example.GYM_management_api.entities.Category;
import com.example.GYM_management_api.entities.enums.CommonStatus;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category toEntity(CategoryDto dto) {
        if (dto == null) {
            return null;
        }

        String code = dto.getCode();
        if (code == null || code.trim().isEmpty()) {
            code = "CAT" + String.format("%03d", (System.currentTimeMillis() % 1000));
        }

        CommonStatus status = CommonStatus.ACTIVE;
        if (dto.getStatus() != null) {
            try {
                status = CommonStatus.valueOf(dto.getStatus().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        Category category = Category.builder()
                .code(code.trim().toUpperCase())
                .name(dto.getName())
                .description(dto.getDescription())
                .status(status)
                .build();

        if (dto.getId() != null && !dto.getId().trim().isEmpty()) {
            try {
                category.setId(Long.parseLong(dto.getId().trim()));
            } catch (NumberFormatException ignored) {}
        }

        return category;
    }

    public void updateEntityFromDto(CategoryDto dto, Category entity) {
        if (dto == null || entity == null) {
            return;
        }

        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        if (dto.getCode() != null && !dto.getCode().trim().isEmpty()) {
            entity.setCode(dto.getCode().trim().toUpperCase());
        }
        if (dto.getStatus() != null) {
            try {
                entity.setStatus(CommonStatus.valueOf(dto.getStatus().toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public CategoryDto toDto(Category entity) {
        if (entity == null) {
            return null;
        }

        return CategoryDto.builder()
                .id(entity.getId() != null ? String.valueOf(entity.getId()) : null)
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .status(entity.getStatus() != null ? entity.getStatus().name() : "ACTIVE")
                .build();
    }
}
