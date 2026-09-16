package com.example.GYM_management_api.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerDto {
    private String id;
    private String code;

    @NotBlank(message = "Tên huấn luyện viên không được để trống")
    @Size(max = 100, message = "Tên không được vượt quá 100 ký tự")
    private String name;

    private String phone;
    private String email;
    private String specialty;
    private String experience;
    private BigDecimal pricePerSession;
    private String avatar;
    private String bio;
    private String status;
    private LocalDateTime createdAt;
}
