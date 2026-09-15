package com.example.GYM_management_api.mappers;

import com.example.GYM_management_api.dtos.PromotionDto;
import com.example.GYM_management_api.entities.Promotion;
import com.example.GYM_management_api.entities.enums.CommonStatus;
import com.example.GYM_management_api.entities.enums.DiscountType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class PromotionMapper {

    public Promotion toEntity(PromotionDto dto) {
        if (dto == null) {
            return null;
        }

        DiscountType discountType = DiscountType.PERCENTAGE;
        if (dto.getDiscountType() != null) {
            try {
                discountType = DiscountType.valueOf(dto.getDiscountType().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        CommonStatus status = CommonStatus.ACTIVE;
        if (dto.getStatus() != null) {
            try {
                status = CommonStatus.valueOf(dto.getStatus().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        Promotion promo = Promotion.builder()
                .code(dto.getCode() != null ? dto.getCode().trim().toUpperCase() : "")
                .name(dto.getName())
                .discountType(discountType)
                .discountValue(dto.getDiscountValue() != null ? dto.getDiscountValue() : BigDecimal.ZERO)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .usageLimit(dto.getUsageLimit() != null ? dto.getUsageLimit() : 100)
                .usedCount(dto.getUsedCount() != null ? dto.getUsedCount() : 0)
                .status(status)
                .build();

        if (dto.getId() != null && !dto.getId().trim().isEmpty()) {
            try {
                promo.setId(Long.parseLong(dto.getId().trim()));
            } catch (NumberFormatException ignored) {}
        }

        return promo;
    }

    public void updateEntityFromDto(PromotionDto dto, Promotion entity) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setCode(dto.getCode() != null ? dto.getCode().trim().toUpperCase() : entity.getCode());
        entity.setName(dto.getName());
        if (dto.getDiscountType() != null) {
            try {
                entity.setDiscountType(DiscountType.valueOf(dto.getDiscountType().toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }
        if (dto.getDiscountValue() != null) {
            entity.setDiscountValue(dto.getDiscountValue());
        }
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        if (dto.getUsageLimit() != null) {
            entity.setUsageLimit(dto.getUsageLimit());
        }
        if (dto.getStatus() != null) {
            try {
                entity.setStatus(CommonStatus.valueOf(dto.getStatus().toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public PromotionDto toDto(Promotion entity) {
        if (entity == null) {
            return null;
        }

        String effectiveStatus = calculateEffectiveStatus(entity);

        return PromotionDto.builder()
                .id(entity.getId() != null ? String.valueOf(entity.getId()) : null)
                .code(entity.getCode())
                .name(entity.getName())
                .discountType(entity.getDiscountType() != null ? entity.getDiscountType().name() : "PERCENTAGE")
                .discountValue(entity.getDiscountValue())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .usageLimit(entity.getUsageLimit())
                .usedCount(entity.getUsedCount())
                .status(entity.getStatus() != null ? entity.getStatus().name() : "ACTIVE")
                .effectiveStatus(effectiveStatus)
                .build();
    }

    public String calculateEffectiveStatus(Promotion promo) {
        LocalDate today = LocalDate.now();
        if (promo.getStatus() == CommonStatus.INACTIVE) {
            return "INACTIVE";
        }
        if (promo.getUsedCount() >= promo.getUsageLimit()) {
            return "USED_UP";
        }
        if (today.isBefore(promo.getStartDate())) {
            return "UPCOMING";
        }
        if (today.isAfter(promo.getEndDate())) {
            return "EXPIRED";
        }
        return "ACTIVE";
    }
}
