package com.example.GYM_management_api.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffDto {
    private String id;
    private String code;

    @NotBlank(message = "Tên nhân viên không được để trống")
    private String name;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    private String phone;

    private String password;

    private String role; // ADMIN, STAFF, TRAINER, MANAGER

    private String status; // ACTIVE, LOCKED, INACTIVE

    private LocalDate createdDate;

    private LocalDateTime lastLogin;
}
