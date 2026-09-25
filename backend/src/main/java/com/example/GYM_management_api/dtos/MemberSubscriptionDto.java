package com.example.GYM_management_api.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin lịch sử đăng ký gói tập của hội viên")
public class MemberSubscriptionDto {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "ID đăng ký", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private Long id;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Mã đăng ký", accessMode = Schema.AccessMode.READ_ONLY, example = "SUB-0001")
    private String code;

    @Schema(description = "ID hội viên", example = "1")
    @com.fasterxml.jackson.annotation.JsonAlias({"member_id"})
    private Long memberId;

    @Schema(description = "Mã hội viên", example = "M0001")
    @com.fasterxml.jackson.annotation.JsonAlias({"member_code"})
    private String memberCode;

    @Schema(description = "ID gói tập đã đăng ký (hoặc packageId)", example = "1")
    @com.fasterxml.jackson.annotation.JsonAlias({"packageId", "package_id", "membership_id"})
    private Long membershipId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Tên gói tập", accessMode = Schema.AccessMode.READ_ONLY, example = "Gói 1 tháng")
    private String membershipName;

    @Schema(description = "Phương thức thanh toán (CASH, BANK_TRANSFER, CARD, E_WALLET)", example = "CASH")
    @com.fasterxml.jackson.annotation.JsonAlias({"payment_method"})
    private String paymentMethod;

    @Schema(description = "Ngày bắt đầu", example = "2026-09-16")
    private LocalDate startDate;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Ngày kết thúc / hết hạn (Tự động tính theo thời hạn gói)", accessMode = Schema.AccessMode.READ_ONLY, example = "2026-10-16")
    private LocalDate endDate;

    @Schema(description = "Số tiền thực tế đã thanh toán", example = "270000.00")
    private BigDecimal paidPrice;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Trạng thái gói tập (ACTIVE, EXPIRED, CANCELLED, PENDING)", accessMode = Schema.AccessMode.READ_ONLY, example = "ACTIVE")
    private String status;

    @Schema(description = "Ghi chú", example = "Đăng ký tại quầy lễ tân")
    private String notes;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Thời điểm đăng ký", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;
}
