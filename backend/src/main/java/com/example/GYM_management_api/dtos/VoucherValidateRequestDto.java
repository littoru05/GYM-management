package com.example.GYM_management_api.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu kiểm tra hợp lệ của mã voucher")
public class VoucherValidateRequestDto {

    @NotBlank(message = "Mã voucher không được để trống")
    @JsonAlias({"voucherCode", "voucher_code"})
    @Schema(description = "Mã voucher", example = "WELCOME10")
    private String code;

    @Schema(description = "ID gói tập hội viên lựa chọn", example = "1")
    @JsonAlias({"membership_id", "packageId", "package_id"})
    private Long membershipId;

    @Schema(description = "Tổng giá trị đơn hàng / giá gói tập trước khi giảm (VND)", example = "250000")
    @JsonAlias({"order_amount", "price", "amount"})
    private BigDecimal orderAmount;

    @Schema(description = "ID hội viên (tùy chọn)", example = "1")
    @JsonAlias({"member_id"})
    private Long memberId;
}
