package com.example.GYM_management_api.mappers;

import com.example.GYM_management_api.dtos.NotificationDto;
import com.example.GYM_management_api.entities.Notification;
import com.example.GYM_management_api.entities.enums.NotificationType;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public Notification toEntity(NotificationDto dto) {
        if (dto == null) {
            return null;
        }

        NotificationType type = NotificationType.SYSTEM;
        if (dto.getType() != null) {
            try {
                type = NotificationType.valueOf(dto.getType().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        Notification entity = Notification.builder()
                .title(dto.getTitle() != null ? dto.getTitle() : "")
                .message(dto.getMessage() != null ? dto.getMessage() : "")
                .type(type)
                .readStatus(Boolean.TRUE.equals(dto.getRead()))
                .actionUrl(dto.getActionUrl())
                .build();

        if (dto.getId() != null && !dto.getId().trim().isEmpty()) {
            try {
                entity.setId(Long.parseLong(dto.getId().trim()));
            } catch (NumberFormatException ignored) {}
        }

        return entity;
    }

    public NotificationDto toDto(Notification entity) {
        if (entity == null) {
            return null;
        }

        return NotificationDto.builder()
                .id(entity.getId() != null ? String.valueOf(entity.getId()) : null)
                .title(entity.getTitle())
                .message(entity.getMessage())
                .type(entity.getType() != null ? entity.getType().name() : "SYSTEM")
                .read(Boolean.TRUE.equals(entity.getReadStatus()))
                .actionUrl(entity.getActionUrl())
                .createdAt(entity.getCreatedAt())
                .time(entity.getCreatedAt())
                .build();
    }
}
