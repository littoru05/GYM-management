package com.example.GYM_management_api.dtos;

import com.example.GYM_management_api.entities.enums.StaffRole;
import com.example.GYM_management_api.entities.enums.StaffStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Thông tin phản hồi đăng nhập / xác thực thành công")
public class AuthResponseDto {

    @Schema(description = "JWT Access Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Builder.Default
    @Schema(description = "Loại Token", example = "Bearer")
    private String type = "Bearer";

    @Schema(description = "ID người dùng", example = "1")
    private Long id;

    @Schema(description = "Mã nhân viên", example = "STF001")
    private String code;

    @Schema(description = "Họ và tên", example = "Quản Trị Viên")
    private String name;

    @Schema(description = "Email tài khoản", example = "admin@gym.com")
    private String email;

    @Schema(description = "Số điện thoại", example = "0901234567")
    private String phone;

    @Schema(description = "Vai trò (ADMIN, STAFF, TRAINER, MANAGER)", example = "ADMIN")
    private StaffRole role;

    @Schema(description = "Trạng thái tài khoản", example = "ACTIVE")
    private StaffStatus status;

    public AuthResponseDto(String token) {
        this.token = token;
        this.type = "Bearer";
    }
}
