package com.example.GYM_management_api.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private String id;
    private String title;
    private String message;
    private String type;
    private Boolean read;
    private String actionUrl;
    private LocalDateTime createdAt;
    private LocalDateTime time;
}
