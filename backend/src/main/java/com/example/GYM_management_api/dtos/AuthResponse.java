package com.example.GYM_management_api.dtos;

import com.example.GYM_management_api.entities.enums.StaffRole;
import com.example.GYM_management_api.entities.enums.StaffStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;

    @Builder.Default
    private String type = "Bearer";

    private Long id;
    private String code;
    private String name;
    private String email;
    private String phone;
    private StaffRole role;
    private StaffStatus status;

    public AuthResponse(String token) {
        this.token = token;
        this.type = "Bearer";
    }
}
