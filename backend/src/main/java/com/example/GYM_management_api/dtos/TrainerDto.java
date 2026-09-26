package com.example.GYM_management_api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Thông tin huấn luyện viên (PT)")
public class TrainerDto {

    @Schema(description = "ID huấn luyện viên", example = "1")
    private String id;

    @Schema(description = "Mã huấn luyện viên", example = "TRN001")
    private String code;

    @NotBlank(message = "Tên huấn luyện viên không được để trống")
    @Size(max = 100, message = "Tên không được vượt quá 100 ký tự")
    @Schema(description = "Tên huấn luyện viên", example = "Trần Văn C")
    private String name;

    @Schema(description = "Số điện thoại", example = "0912345678")
    private String phone;

    @Schema(description = "Email", example = "trainer@gym.com")
    private String email;

    @Schema(description = "Chuyên môn", example = "Thể hình & Giảm cân")
    private String specialty;

    @Schema(description = "Kinh nghiệm làm việc", example = "5 năm kinh nghiệm HLV cá nhân")
    private String experience;

    @Schema(description = "Đơn giá mỗi buổi tập (VND)", example = "300000")
    private BigDecimal pricePerSession;

    @Schema(description = "Ảnh đại diện", example = "https://images.unsplash.com/photo-1567013127542-490d757e51fc")
    private String avatar;

    @Schema(description = "Tiểu sử giới thiệu", example = "Đạt chứng chỉ HLV Quốc Tế ISSA...")
    private String bio;

    @Schema(description = "Trạng thái hoạt động", example = "ACTIVE")
    private String status;

    private LocalDateTime createdAt;
}
