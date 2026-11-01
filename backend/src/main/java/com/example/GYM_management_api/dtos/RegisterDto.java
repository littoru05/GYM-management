package com.example.GYM_management_api.dtos;

import com.example.GYM_management_api.entities.enums.StaffRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin đăng ký tài khoản nhân viên")
public class RegisterDto {

    @NotBlank(message = "Họ và tên không được để trống")
    @Schema(description = "Họ và tên", example = "Nguyễn Văn B")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Schema(description = "Email đăng ký", example = "staff@gym.com")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Schema(description = "Số điện thoại liên hệ", example = "0987654321")
    private String phone;

    @Schema(description = "Mã nhân viên (nếu có)", example = "STF002")
    private String code;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu tối thiểu 6 ký tự")
    @Schema(description = "Mật khẩu tài khoản (tối thiểu 6 ký tự)", example = "123456")
    private String password;

    @Schema(description = "Vai trò nhân viên (ADMIN, STAFF, TRAINER, MANAGER)", example = "STAFF")
    private StaffRole role;
}
