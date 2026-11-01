package com.example.GYM_management_api.mappers;

import com.example.GYM_management_api.dtos.StaffDto;
import com.example.GYM_management_api.entities.Staff;
import com.example.GYM_management_api.entities.enums.StaffRole;
import com.example.GYM_management_api.entities.enums.StaffStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class StaffMapper {

    public Staff toEntity(StaffDto dto) {
        if (dto == null) {
            return null;
        }

        String code = dto.getCode();
        if (code == null || code.trim().isEmpty()) {
            code = "STF-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        } else {
            code = code.trim().toUpperCase();
        }

        StaffRole role = StaffRole.STAFF;
        if (dto.getRole() != null) {
            try {
                role = StaffRole.valueOf(dto.getRole().trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        StaffStatus status = StaffStatus.ACTIVE;
        if (dto.getStatus() != null) {
            try {
                status = StaffStatus.valueOf(dto.getStatus().trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        Staff staff = Staff.builder()
                .code(code)
                .name(dto.getName() != null ? dto.getName().trim() : null)
                .email(dto.getEmail() != null ? dto.getEmail().trim().toLowerCase() : null)
                .phone(dto.getPhone() != null ? dto.getPhone().trim() : null)
                .role(role)
                .status(status)
                .lastLogin(dto.getLastLogin())
                .build();

        if (dto.getId() != null && !dto.getId().trim().isEmpty()) {
            try {
                staff.setId(Long.parseLong(dto.getId().trim()));
            } catch (NumberFormatException ignored) {}
        }

        return staff;
    }

    public void updateEntityFromDto(StaffDto dto, Staff entity) {
        if (dto == null || entity == null) {
            return;
        }

        if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
            entity.setName(dto.getName().trim());
        }
        if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
            entity.setEmail(dto.getEmail().trim().toLowerCase());
        }
        if (dto.getPhone() != null) {
            entity.setPhone(dto.getPhone().trim());
        }
        if (dto.getRole() != null && !dto.getRole().trim().isEmpty()) {
            try {
                entity.setRole(StaffRole.valueOf(dto.getRole().trim().toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }
        if (dto.getStatus() != null && !dto.getStatus().trim().isEmpty()) {
            try {
                entity.setStatus(StaffStatus.valueOf(dto.getStatus().trim().toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }
        if (dto.getCode() != null && !dto.getCode().trim().isEmpty()) {
            entity.setCode(dto.getCode().trim().toUpperCase());
        }
    }

    public StaffDto toDto(Staff entity) {
        if (entity == null) {
            return null;
        }

        return StaffDto.builder()
                .id(entity.getId() != null ? String.valueOf(entity.getId()) : null)
                .code(entity.getCode())
                .name(entity.getName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .role(entity.getRole() != null ? entity.getRole().name() : "STAFF")
                .status(entity.getStatus() != null ? entity.getStatus().name() : "ACTIVE")
                .createdDate(entity.getCreatedAt() != null ? entity.getCreatedAt().toLocalDate() : null)
                .lastLogin(entity.getLastLogin())
                .build();
    }
}
