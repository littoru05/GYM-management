package com.example.GYM_management_api.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Thông tin tài khoản nhân viên")
public class StaffDto {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "ID nhân viên", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private String id;

    @Schema(description = "Mã nhân viên (Hệ thống tự sinh nếu để trống)", example = "STF-A1B2C3")
    private String code;

    @NotBlank(message = "Tên nhân viên không được để trống")
    @Schema(description = "Họ và tên nhân viên", example = "Nguyễn Văn B")
    private String name;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Schema(description = "Email nhân viên (duy nhất)", example = "staff@gym.com")
    private String email;

    @Pattern(regexp = "^$|^(0|\\+84)[0-9]{9,10}$", message = "Số điện thoại không đúng định dạng chuẩn (10 chữ số bắt đầu bằng 0 hoặc +84)")
    @Schema(description = "Số điện thoại liên hệ", example = "0912345678")
    private String phone;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Schema(description = "Mật khẩu đăng nhập (chỉ nhận khi tạo/sửa, không bao giờ hiển thị ra ngoài)", example = "123456")
    private String password;

    @Schema(description = "Vai trò (ADMIN, STAFF)", example = "STAFF")
    private String role;

    @Schema(description = "Trạng thái tài khoản (ACTIVE, LOCKED, INACTIVE)", example = "ACTIVE")
    private String status;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Ngày tạo tài khoản", accessMode = Schema.AccessMode.READ_ONLY, example = "2026-10-16")
    private LocalDate createdDate;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Thời gian đăng nhập gần nhất", accessMode = Schema.AccessMode.READ_ONLY, example = "2026-10-16T08:00:00")
    private LocalDateTime lastLogin;
}
