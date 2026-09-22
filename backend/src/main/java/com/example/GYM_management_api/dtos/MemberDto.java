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
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Thông tin chi tiết hồ sơ hội viên")
public class MemberDto {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "ID hội viên", accessMode = Schema.AccessMode.READ_ONLY)
    private String id;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Mã hội viên duy nhất (Tự sinh)", accessMode = Schema.AccessMode.READ_ONLY)
    private String code;

    @NotBlank(message = "Tên hội viên không được để trống")
    @Schema(description = "Họ và tên hội viên")
    private String name;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0[0-9]{9}$", message = "Số điện thoại không đúng định dạng chuẩn Việt Nam (10 chữ số bắt đầu bằng số 0)")
    @Schema(description = "Số điện thoại liên hệ")
    private String phone;

    @Email(message = "Định dạng email không hợp lệ")
    @Schema(description = "Email hội viên")
    private String email;

    @Schema(description = "Ngày tháng năm sinh (yyyy-MM-dd)")
    private LocalDate dob;

    @Schema(description = "Ngày tham gia (yyyy-MM-dd)")
    private LocalDate joinDate;

    @Schema(description = "Giới tính (Nam / Nữ / Khác)")
    private String gender;

    @Schema(description = "Địa chỉ liên lạc")
    private String address;

    @Schema(description = "Ghi chú sức khỏe")
    private String healthNotes;

    @Schema(description = "ID gói tập đăng ký (nếu có)")
    private String membershipId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Tên gói tập hiện tại", accessMode = Schema.AccessMode.READ_ONLY)
    private String membershipName;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Ngày hết hạn gói tập gần nhất", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDate expiryDate;

    @Schema(description = "Trạng thái hoạt động (ACTIVE, EXPIRING_SOON, EXPIRED, LOCKED)")
    private String status;

    @Schema(description = "Ảnh đại diện avatar (URL)")
    private String avatar;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Lịch sử tất cả các gói tập hội viên đã đăng ký", accessMode = Schema.AccessMode.READ_ONLY)
    private List<MemberSubscriptionDto> subscriptions;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Thời điểm tạo hồ sơ", accessMode = Schema.AccessMode.READ_ONLY)
    private java.time.LocalDateTime createdAt;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Thời điểm cập nhật gần nhất", accessMode = Schema.AccessMode.READ_ONLY)
    private java.time.LocalDateTime updatedAt;

    public List<MemberSubscriptionDto> getMembershipHistory() {
        return subscriptions;
    }

    public void setMembershipHistory(List<MemberSubscriptionDto> history) {
        this.subscriptions = history;
    }

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(java.time.LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setCreatedAt(String val) {
        this.createdAt = parseIsoDateTime(val);
    }

    public void setUpdatedAt(String val) {
        this.updatedAt = parseIsoDateTime(val);
    }

    private static java.time.LocalDateTime parseIsoDateTime(String val) {
        if (val == null || val.trim().isEmpty())
            return null;
        try {
            if (val.endsWith("Z") || val.contains("+")) {
                return java.time.OffsetDateTime.parse(val).atZoneSameInstant(java.time.ZoneId.systemDefault())
                        .toLocalDateTime();
            }
            return java.time.LocalDateTime.parse(val);
        } catch (Exception e) {
            return java.time.LocalDateTime.now();
        }
    }
}
