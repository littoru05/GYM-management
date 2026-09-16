package com.example.GYM_management_api.mappers;

import com.example.GYM_management_api.dtos.ProductDto;
import com.example.GYM_management_api.entities.Category;
import com.example.GYM_management_api.entities.Product;
import com.example.GYM_management_api.entities.enums.CommonStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ProductMapper {

    public Product toEntity(ProductDto dto, Category category) {
        if (dto == null) {
            return null;
        }

        String sku = dto.getSku();
        if (sku == null || sku.trim().isEmpty()) {
            String catCode = (category != null ? category.getCode() : "GEN");
            sku = "SKU-" + catCode.substring(0, Math.min(3, catCode.length())).toUpperCase() + "-" + (System.currentTimeMillis() % 10000);
        }

        CommonStatus status = CommonStatus.ACTIVE;
        if (dto.getStatus() != null) {
            try {
                status = CommonStatus.valueOf(dto.getStatus().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        Product product = Product.builder()
                .sku(sku)
                .name(dto.getName())
                .category(category)
                .price(dto.getPrice() != null ? dto.getPrice() : BigDecimal.ZERO)
                .cost(dto.getCost() != null ? dto.getCost() : BigDecimal.ZERO)
                .stock(dto.getStock() != null ? dto.getStock() : 0)
                .minStock(dto.getMinStock() != null ? dto.getMinStock() : 10)
                .status(status)
                .image(dto.getImage() != null ? dto.getImage() : "https://picsum.photos/seed/" + sku + "/300/300")
                .build();

        if (dto.getId() != null && !dto.getId().trim().isEmpty()) {
            try {
                product.setId(Long.parseLong(dto.getId().trim()));
            } catch (NumberFormatException ignored) {}
        }

        return product;
    }

    public void updateEntityFromDto(ProductDto dto, Product entity, Category category) {
        if (dto == null || entity == null) {
            return;
        }

        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        if (category != null) {
            entity.setCategory(category);
        }
        if (dto.getPrice() != null) {
            entity.setPrice(dto.getPrice());
        }
        if (dto.getCost() != null) {
            entity.setCost(dto.getCost());
        }
        if (dto.getSku() != null && !dto.getSku().isEmpty()) {
            entity.setSku(dto.getSku());
        }
        if (dto.getStock() != null) {
            entity.setStock(dto.getStock());
        }
        if (dto.getMinStock() != null) {
            entity.setMinStock(dto.getMinStock());
        }
        if (dto.getStatus() != null) {
            try {
                entity.setStatus(CommonStatus.valueOf(dto.getStatus().toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }
        if (dto.getImage() != null) {
            entity.setImage(dto.getImage());
        }
    }

    public ProductDto toDto(Product entity) {
        if (entity == null) {
            return null;
        }

        Category cat = entity.getCategory();
        String catDisplay = cat != null ? cat.getCode() : "GENERAL";
        String catId = cat != null ? String.valueOf(cat.getId()) : null;

        return ProductDto.builder()
                .id(entity.getId() != null ? String.valueOf(entity.getId()) : null)
                .sku(entity.getSku())
                .name(entity.getName())
                .category(catDisplay)
                .categoryId(catId)
                .price(entity.getPrice())
                .cost(entity.getCost())
                .stock(entity.getStock())
                .minStock(entity.getMinStock())
                .status(entity.getStatus() != null ? entity.getStatus().name() : "ACTIVE")
                .image(entity.getImage())
                .build();
    }
}
