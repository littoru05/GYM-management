package com.example.GYM_management_api.controllers;

import com.example.GYM_management_api.dtos.ErrorResponseDto;
import com.example.GYM_management_api.dtos.VoucherValidateRequestDto;
import com.example.GYM_management_api.dtos.VoucherValidateResponseDto;
import com.example.GYM_management_api.services.IVoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
@Tag(name = "Voucher & Khuyến Mãi (Vouchers)", description = "APIs kiểm tra và áp dụng mã voucher giảm giá khi mua gói tập hoặc thanh toán")
@SecurityRequirement(name = "Bearer Authentication")
public class VoucherController {

    private final IVoucherService voucherService;

    @PostMapping("/validate")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Kiểm tra tính hợp lệ của mã voucher",
            description = "Kiểm tra mã voucher, hạn dùng, số lượt, điều kiện gói tập, tính số tiền giảm và giá sau giảm.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Voucher hợp lệ",
                    content = @Content(schema = @Schema(implementation = VoucherValidateResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Voucher không hợp lệ hoặc không đủ điều kiện",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy mã voucher",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<VoucherValidateResponseDto> validateVoucher(
            @Valid @RequestBody VoucherValidateRequestDto request) {
        VoucherValidateResponseDto response = voucherService.validateVoucher(request);
        return ResponseEntity.ok(response);
    }
}
