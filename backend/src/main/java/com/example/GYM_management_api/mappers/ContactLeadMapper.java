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

        LeadStatus status = LeadStatus.NEW;
        if (dto.getStatus() != null && !dto.getStatus().trim().isEmpty()) {
            try {
                status = LeadStatus.valueOf(dto.getStatus().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        String combinedMessage = dto.getMessage();
        if (dto.getService() != null && !dto.getService().trim().isEmpty()) {
            String serviceTag = "[Dịch vụ: " + dto.getService().trim() + "]";
            if (combinedMessage == null || combinedMessage.trim().isEmpty()) {
                combinedMessage = serviceTag;
            } else if (!combinedMessage.contains(dto.getService().trim())) {
                combinedMessage = serviceTag + " " + combinedMessage;
            }
        }

        ContactLead entity = ContactLead.builder()
                .name(dto.getName() != null ? dto.getName().trim() : "")
                .phone(dto.getPhone() != null ? dto.getPhone().trim() : "")
                .email(dto.getEmail() != null ? dto.getEmail().trim() : null)
                .message(combinedMessage)
                .status(status)
                .isRead(dto.getIsRead() != null ? dto.getIsRead() : false)
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

        String extractedService = null;
        if (entity.getMessage() != null && entity.getMessage().contains("[Dịch vụ: ")) {
            int start = entity.getMessage().indexOf("[Dịch vụ: ") + 10;
            int end = entity.getMessage().indexOf("]", start);
            if (end > start) {
                extractedService = entity.getMessage().substring(start, end).trim();
            }
        }

        return ContactLeadDto.builder()
                .id(entity.getId() != null ? String.valueOf(entity.getId()) : null)
                .name(entity.getName())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .service(extractedService)
                .message(entity.getMessage())
                .status(entity.getStatus() != null ? entity.getStatus().name() : "NEW")
                .isRead(entity.getIsRead() != null ? entity.getIsRead() : false)
                .notes(entity.getNotes())
                .assignedStaffName(entity.getAssignedStaffName())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
