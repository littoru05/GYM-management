package com.example.GYM_management_api.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin lượt điểm danh check-in tại quầy lễ tân")
public class CheckInDto {

    @Schema(description = "Số điện thoại hội viên hoặc Mã hội viên (quét QR / nhập tại quầy)", example = "0987654321")
    @JsonAlias({"identifier", "phoneNumber", "phone_number", "memberCode", "member_code"})
    private String phone;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "ID lượt check-in", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private String id;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Mã lượt check-in", accessMode = Schema.AccessMode.READ_ONLY, example = "CI-123456")
    private String code;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "ID hội viên", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    @JsonAlias({"member_id"})
    private String memberId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Họ và tên hội viên", accessMode = Schema.AccessMode.READ_ONLY, example = "Nguyễn Văn A")
    private String memberName;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Ảnh đại diện hội viên", accessMode = Schema.AccessMode.READ_ONLY, example = "https://i.pravatar.cc/150?u=M0001")
    private String avatar;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Tên gói tập đang sử dụng", accessMode = Schema.AccessMode.READ_ONLY, example = "Gói 1 tháng")
    private String membershipName;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Thời điểm quẹt thẻ check-in", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonAlias({"checkInTime", "check_in_time"})
    private LocalDateTime time;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Thời điểm check-out (nếu có)", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonAlias({"check_out_time"})
    private LocalDateTime checkOutTime;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Trạng thái điểm danh (SUCCESS, DENIED)", accessMode = Schema.AccessMode.READ_ONLY, example = "SUCCESS")
    private String status;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Lý do từ chối hoặc thông tin bổ sung", accessMode = Schema.AccessMode.READ_ONLY, example = "Điểm danh thành công")
    private String reason;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Cờ mức độ cảnh báo cho giao diện FE: GREEN (Hợp lệ), YELLOW (Sắp hết hạn <= 7 ngày), RED (Từ chối)", accessMode = Schema.AccessMode.READ_ONLY, example = "GREEN")
    private String warningLevel;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "ID nhân viên trực quầy", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    @JsonAlias({"staff_id"})
    private Long staffId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Mã nhân viên trực quầy", accessMode = Schema.AccessMode.READ_ONLY, example = "STAFF001")
    @JsonAlias({"staff_code"})
    private String staffCode;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Họ tên nhân viên trực quầy", accessMode = Schema.AccessMode.READ_ONLY, example = "Trần Thị Thu Ngân")
    @JsonAlias({"staff_name"})
    private String staffName;
}
