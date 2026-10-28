package com.example.GYM_management_api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin liên hệ tư vấn")
public class ContactLeadDto {

    @Schema(description = "ID liên hệ", accessMode = Schema.AccessMode.READ_ONLY)
    private String id;

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(min = 2, max = 50, message = "Họ và tên phải từ 2 đến 50 ký tự")
    @Pattern(regexp = "^[a-zA-ZÀ-ỹ\\s]+$", message = "Họ và tên chỉ được chứa chữ cái, không được chứa số hoặc ký tự đặc biệt")
    @Schema(description = "Họ và tên khách hàng", example = "Nguyen Van A")
    private String name;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0[3|5|7|8|9])[0-9]{8}$", message = "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 03, 05, 07, 08 hoặc 09")
    @Schema(description = "Số điện thoại liên hệ (10 chữ số)", example = "0901234567")
    private String phone;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@gmail\\.com$", message = "Email phải đúng định dạng và có đuôi @gmail.com (ví dụ: yourname@gmail.com)")
    @Schema(description = "Địa chỉ email (đuôi @gmail.com)", example = "testlead1@gmail.com")
    private String email;

    @Size(max = 500, message = "Nội dung tư vấn tối đa 500 ký tự")
    @Schema(description = "Nội dung nhu cầu tư vấn", example = "Tôi cần tư vấn gói tập Gym 3 tháng")
    private String message;

    @Schema(description = "Trạng thái xử lý", accessMode = Schema.AccessMode.READ_ONLY)
    private String status; // NEW, CONTACTED, PENDING, CONVERTED, CANCELLED

    @Schema(description = "Trạng thái đã đọc", accessMode = Schema.AccessMode.READ_ONLY)
    private Boolean isRead;

    @Schema(description = "Ghi chú của nhân viên", accessMode = Schema.AccessMode.READ_ONLY)
    private String notes;

    @Schema(description = "Nhân viên phụ trách", accessMode = Schema.AccessMode.READ_ONLY)
    private String assignedStaffName;

    @Schema(description = "Thời gian tạo yêu cầu", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;
}
