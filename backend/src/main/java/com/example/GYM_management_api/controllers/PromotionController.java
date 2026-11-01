package com.example.GYM_management_api.controllers;

import com.example.GYM_management_api.dtos.ErrorResponseDto;
import com.example.GYM_management_api.dtos.PromotionDto;
import com.example.GYM_management_api.dtos.VoucherApplyRequestDto;
import com.example.GYM_management_api.dtos.VoucherApplyResponseDto;
import com.example.GYM_management_api.services.IPromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Khuyến Mãi & Voucher (Promotions)", description = "APIs CRUD chiến dịch khuyến mãi/voucher cho Admin và kiểm tra áp dụng mã giảm giá cho POS/Hội viên")
@SecurityRequirement(name = "Bearer Authentication")
public class PromotionController {

    private final IPromotionService promotionService;

    // =========================================================================
    // 1. OPERATION ENDPOINTS (STAFF / ADMIN / POS CHECKOUT)
    // =========================================================================

    @PostMapping({"/promotions/apply", "/vouchers/apply"})
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Kiểm tra và áp dụng mã giảm giá", description = "Kiểm tra tính hợp lệ của mã giảm giá (tồn tại, thời hạn, số lượt dùng, đơn tối thiểu) và tính số tiền giảm.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Áp dụng mã giảm giá thành công", content = @Content(schema = @Schema(implementation = VoucherApplyResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Mã không đủ điều kiện (hết hạn, hết lượt, chưa đủ tiền tối thiểu, v.v.)", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy mã giảm giá", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<VoucherApplyResponseDto> applyPromotion(
            @Valid @RequestBody VoucherApplyRequestDto request) {
        VoucherApplyResponseDto response = promotionService.applyVoucher(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping({"/promotions/active", "/vouchers/active"})
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Lấy danh sách mã khuyến mãi đang hoạt động")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công", content = @Content(array = @ArraySchema(schema = @Schema(implementation = PromotionDto.class))))
    })
    public ResponseEntity<List<PromotionDto>> getActivePromotions() {
        List<PromotionDto> list = promotionService.getAllActivePromotions();
        return ResponseEntity.ok(list);
    }

    // =========================================================================
    // 2. ADMIN CRUD ENDPOINTS (ADMIN ONLY)
    // =========================================================================

    @GetMapping({"/admin/promotions", "/admin/vouchers"})
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy danh sách chiến dịch khuyến mãi (phân trang & lọc)", description = "Dành riêng cho Admin quản lý các chiến dịch khuyến mãi.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<Page<PromotionDto>> getPromotions(
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng bản ghi mỗi trang", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Từ khóa tìm kiếm theo mã hoặc tên khuyến mãi", example = "PROMO") @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String search,
            @Parameter(description = "Lọc theo trạng thái (ACTIVE, INACTIVE)", example = "ACTIVE") @RequestParam(required = false) String status) {
        String searchKeyword = (keyword != null && !keyword.isBlank()) ? keyword : search;
        Page<PromotionDto> result = promotionService.getPromotions(page, size, searchKeyword, status);
        return ResponseEntity.ok(result);
    }

    @GetMapping({"/admin/promotions/{id}", "/admin/vouchers/{id}"})
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy chi tiết chiến dịch khuyến mãi theo ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy chi tiết thành công", content = @Content(schema = @Schema(implementation = PromotionDto.class))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy chương trình khuyến mãi", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<PromotionDto> getPromotionById(
            @Parameter(description = "ID chương trình khuyến mãi", example = "1") @PathVariable Long id) {
        PromotionDto result = promotionService.getPromotionById(id);
        return ResponseEntity.ok(result);
    }

    @PostMapping({"/admin/promotions", "/admin/vouchers"})
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tạo mới chiến dịch khuyến mãi")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tạo chiến dịch khuyến mãi thành công", content = @Content(schema = @Schema(implementation = PromotionDto.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "409", description = "Trùng mã khuyến mãi", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<PromotionDto> createPromotion(@Valid @RequestBody PromotionDto request) {
        PromotionDto created = promotionService.createPromotion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping({"/admin/promotions/{id}", "/admin/vouchers/{id}"})
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật chiến dịch khuyến mãi")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công", content = @Content(schema = @Schema(implementation = PromotionDto.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy chương trình khuyến mãi", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "409", description = "Trùng mã khuyến mãi với chiến dịch khác", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<PromotionDto> updatePromotion(
            @Parameter(description = "ID chương trình khuyến mãi", example = "1") @PathVariable Long id,
            @Valid @RequestBody PromotionDto request) {
        PromotionDto updated = promotionService.updatePromotion(id, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping({"/admin/promotions/{id}/toggle-status", "/admin/vouchers/{id}/toggle-status"})
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Chuyển đổi trạng thái chiến dịch khuyến mãi")
    public ResponseEntity<PromotionDto> togglePromotionStatus(
            @Parameter(description = "ID chương trình khuyến mãi", example = "1") @PathVariable Long id) {
        PromotionDto current = promotionService.getPromotionById(id);
        String nextStatus = "ACTIVE".equalsIgnoreCase(current.getStatus()) ? "INACTIVE" : "ACTIVE";
        current.setStatus(nextStatus);
        PromotionDto updated = promotionService.updatePromotion(id, current);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping({"/admin/promotions/{id}", "/admin/vouchers/{id}"})
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa chiến dịch khuyến mãi")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Xóa thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy chương trình khuyến mãi", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<Void> deletePromotion(
            @Parameter(description = "ID chương trình khuyến mãi", example = "1") @PathVariable Long id) {
        promotionService.deletePromotion(id);
        return ResponseEntity.noContent().build();
    }
}
