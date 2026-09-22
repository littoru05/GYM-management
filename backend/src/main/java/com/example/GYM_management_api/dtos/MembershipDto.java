package com.example.GYM_management_api.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin gói tập phòng GYM")
public class MembershipDto {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "ID của gói tập", accessMode = Schema.AccessMode.READ_ONLY)
    private String id;

    @Schema(description = "Mã gói tập duy nhất")
    private String code;

    @NotBlank(message = "Tên gói tập không được để trống")
    @Size(max = 100, message = "Tên gói tập không được vượt quá 100 ký tự")
    @Schema(description = "Tên gói tập", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotNull(message = "Thời hạn gói tập không được để trống")
    @Min(value = 1, message = "Thời hạn gói tập phải lớn hơn 0")
    @Schema(description = "Thời hạn gói tập (tính theo tháng)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer durationMonths;

    @NotNull(message = "Giá gói tập không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Giá niêm yết không được âm")
    @Schema(description = "Đơn giá niêm yết (VND)", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal price;

    @Schema(description = "Mô tả chi tiết quyền lợi gói tập")
    private String description;

    @Schema(description = "Danh sách quyền lợi đi kèm")
    private List<String> benefits;

    @Schema(description = "Trạng thái gói tập (ACTIVE: Đang bán, INACTIVE: Tạm ngưng)")
    private String status;

    @Schema(description = "Gói tập phổ biến/nổi bật")
    private Boolean popular;

    @Schema(hidden = true)
    public void setDuration(Integer duration) {
        if (duration != null && (this.durationMonths == null || this.durationMonths == 0)) {
            this.durationMonths = duration;
        }
    }

    @Schema(hidden = true)
    public Integer getDuration() {
        return this.durationMonths;
    }
}
