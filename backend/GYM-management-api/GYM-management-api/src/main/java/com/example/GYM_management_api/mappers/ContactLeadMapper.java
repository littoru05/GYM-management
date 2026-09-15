package com.example.GYM_management_api.mappers;

import com.example.GYM_management_api.dtos.ContactLeadDto;
import com.example.GYM_management_api.entities.ContactLead;
import com.example.GYM_management_api.entities.enums.LeadStatus;
import org.springframework.stereotype.Component;

@Component
public class ContactLeadMapper {

    public ContactLead toEntity(ContactLeadDto dto) {
        if (dto == null) {
            return null;
        }

        LeadStatus status = LeadStatus.PENDING;
        if (dto.getStatus() != null && !dto.getStatus().trim().isEmpty()) {
            try {
                status = LeadStatus.valueOf(dto.getStatus().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        ContactLead entity = ContactLead.builder()
                .name(dto.getName() != null ? dto.getName().trim() : "")
                .phone(dto.getPhone() != null ? dto.getPhone().trim() : "")
                .email(dto.getEmail() != null ? dto.getEmail().trim() : null)
                .message(dto.getMessage())
                .status(status)
                .notes(dto.getNotes())
                .assignedStaffName(dto.getAssignedStaffName())
                .build();

        if (dto.getId() != null && !dto.getId().trim().isEmpty()) {
            try {
                entity.setId(Long.parseLong(dto.getId().trim()));
            } catch (NumberFormatException ignored) {}
        }

        return entity;
    }

    public ContactLeadDto toDto(ContactLead entity) {
        if (entity == null) {
            return null;
        }

        return ContactLeadDto.builder()
                .id(entity.getId() != null ? String.valueOf(entity.getId()) : null)
                .name(entity.getName())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .message(entity.getMessage())
                .status(entity.getStatus() != null ? entity.getStatus().name() : "PENDING")
                .notes(entity.getNotes())
                .assignedStaffName(entity.getAssignedStaffName())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
