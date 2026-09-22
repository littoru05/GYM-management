package com.example.GYM_management_api.mappers;

import com.example.GYM_management_api.dtos.MembershipDto;
import com.example.GYM_management_api.entities.Membership;
import com.example.GYM_management_api.entities.enums.CommonStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;

@Component
public class MembershipMapper {

    public Membership toEntity(MembershipDto dto) {
        if (dto == null) {
            return null;
        }

        String code = dto.getCode();
        if (code == null || code.trim().isEmpty()) {
            code = "PKG-" + String.format("%04d", (System.currentTimeMillis() % 10000));
        }

        CommonStatus status = CommonStatus.ACTIVE;
        if (dto.getStatus() != null && !dto.getStatus().trim().isEmpty()) {
            try {
                status = CommonStatus.valueOf(dto.getStatus().trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        Integer durationMonths = dto.getDurationMonths();
        if (durationMonths == null && dto.getDuration() != null) {
            durationMonths = dto.getDuration();
        }

        Membership entity = Membership.builder()
                .code(code.trim().toUpperCase())
                .name(dto.getName() != null ? dto.getName().trim() : null)
                .durationMonths(durationMonths != null ? durationMonths : 1)
                .price(dto.getPrice() != null ? dto.getPrice() : BigDecimal.ZERO)
                .description(dto.getDescription())
                .benefits(dto.getBenefits() != null ? new ArrayList<>(dto.getBenefits()) : new ArrayList<>())
                .status(status)
                .popular(dto.getPopular() != null ? dto.getPopular() : false)
                .build();

        if (dto.getId() != null && !dto.getId().trim().isEmpty()) {
            try {
                entity.setId(Long.parseLong(dto.getId().trim()));
            } catch (NumberFormatException ignored) {}
        }

        return entity;
    }

    public void updateEntityFromDto(MembershipDto dto, Membership entity) {
        if (dto == null || entity == null) {
            return;
        }

        if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
            entity.setName(dto.getName().trim());
        }
        Integer durationMonths = dto.getDurationMonths() != null ? dto.getDurationMonths() : dto.getDuration();
        if (durationMonths != null && durationMonths > 0) {
            entity.setDurationMonths(durationMonths);
        }
        if (dto.getPrice() != null) {
            entity.setPrice(dto.getPrice());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        if (dto.getBenefits() != null) {
            entity.setBenefits(new ArrayList<>(dto.getBenefits()));
        }
        if (dto.getStatus() != null && !dto.getStatus().trim().isEmpty()) {
            try {
                entity.setStatus(CommonStatus.valueOf(dto.getStatus().trim().toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }
        if (dto.getPopular() != null) {
            entity.setPopular(dto.getPopular());
        }
        if (dto.getCode() != null && !dto.getCode().trim().isEmpty()) {
            entity.setCode(dto.getCode().trim().toUpperCase());
        }
    }

    public MembershipDto toDto(Membership entity) {
        if (entity == null) {
            return null;
        }

        return MembershipDto.builder()
                .id(entity.getId() != null ? String.valueOf(entity.getId()) : null)
                .code(entity.getCode())
                .name(entity.getName())
                .durationMonths(entity.getDurationMonths())
                .price(entity.getPrice())
                .description(entity.getDescription())
                .benefits(entity.getBenefits() != null ? new ArrayList<>(entity.getBenefits()) : new ArrayList<>())
                .status(entity.getStatus() != null ? entity.getStatus().name() : "ACTIVE")
                .popular(entity.getPopular())
                .build();
    }
}
