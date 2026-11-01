package com.example.GYM_management_api.mappers;

import com.example.GYM_management_api.dtos.TrainerDto;
import com.example.GYM_management_api.entities.Trainer;
import com.example.GYM_management_api.entities.enums.CommonStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TrainerMapper {

    public Trainer toEntity(TrainerDto dto) {
        if (dto == null) {
            return null;
        }

        String code = dto.getCode();
        if (code == null || code.trim().isEmpty()) {
            code = "PT" + (System.currentTimeMillis() % 10000);
        }

        CommonStatus status = CommonStatus.ACTIVE;
        if (dto.getStatus() != null) {
            try {
                status = CommonStatus.valueOf(dto.getStatus().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        Trainer entity = Trainer.builder()
                .code(code.trim().toUpperCase())
                .name(dto.getName() != null ? dto.getName().trim() : "")
                .phone(dto.getPhone() != null ? dto.getPhone().trim() : null)
                .email(dto.getEmail() != null ? dto.getEmail().trim() : null)
                .specialty(dto.getSpecialty() != null ? dto.getSpecialty().trim() : null)
                .experience(dto.getExperience() != null ? dto.getExperience().trim() : null)
                .pricePerSession(dto.getPricePerSession() != null ? dto.getPricePerSession() : BigDecimal.ZERO)
                .avatar(dto.getAvatar() != null ? dto.getAvatar().trim() : null)
                .bio(dto.getBio() != null ? dto.getBio().trim() : null)
                .status(status)
                .build();

        if (dto.getId() != null && !dto.getId().trim().isEmpty()) {
            try {
                entity.setId(Long.parseLong(dto.getId().trim()));
            } catch (NumberFormatException ignored) {}
        }

        return entity;
    }

    public void updateEntityFromDto(TrainerDto dto, Trainer entity) {
        if (dto == null || entity == null) {
            return;
        }

        if (dto.getName() != null) {
            entity.setName(dto.getName().trim());
        }
        if (dto.getPhone() != null) {
            entity.setPhone(dto.getPhone().trim());
        }
        if (dto.getEmail() != null) {
            entity.setEmail(dto.getEmail().trim());
        }
        if (dto.getSpecialty() != null) {
            entity.setSpecialty(dto.getSpecialty().trim());
        }
        if (dto.getExperience() != null) {
            entity.setExperience(dto.getExperience().trim());
        }
        if (dto.getPricePerSession() != null) {
            entity.setPricePerSession(dto.getPricePerSession());
        }
        if (dto.getAvatar() != null) {
            entity.setAvatar(dto.getAvatar().trim());
        }
        if (dto.getBio() != null) {
            entity.setBio(dto.getBio().trim());
        }
        if (dto.getStatus() != null) {
            try {
                entity.setStatus(CommonStatus.valueOf(dto.getStatus().toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }
        if (dto.getCode() != null && !dto.getCode().trim().isEmpty()) {
            entity.setCode(dto.getCode().trim().toUpperCase());
        }
    }

    public TrainerDto toDto(Trainer entity) {
        if (entity == null) {
            return null;
        }

        return TrainerDto.builder()
                .id(entity.getId() != null ? String.valueOf(entity.getId()) : null)
                .code(entity.getCode())
                .name(entity.getName())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .specialty(entity.getSpecialty())
                .experience(entity.getExperience())
                .pricePerSession(entity.getPricePerSession())
                .avatar(entity.getAvatar())
                .bio(entity.getBio())
                .status(entity.getStatus() != null ? entity.getStatus().name() : "ACTIVE")
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
