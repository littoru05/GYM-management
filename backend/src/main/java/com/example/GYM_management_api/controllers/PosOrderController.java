package com.example.GYM_management_api.controllers;

import com.example.GYM_management_api.dtos.CheckoutRequestDTO;
import com.example.GYM_management_api.dtos.ErrorResponseDto;
import com.example.GYM_management_api.dtos.OrderResponseDTO;
import com.example.GYM_management_api.entities.Staff;
import com.example.GYM_management_api.repositories.StaffRepository;
import com.example.GYM_management_api.services.IPosOrderService;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "POS & Retail Checkout")
@SecurityRequirement(name = "Bearer Authentication")
public class PosOrderController {

    private final IPosOrderService posOrderService;
    private final StaffRepository staffRepository;

    @PostMapping("/checkout")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tạo đơn hàng thành công",
                    content = @Content(schema = @Schema(implementation = OrderResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc thiếu hàng tồn kho",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Từ chối quyền (Chỉ ADMIN, STAFF)",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy sản phẩm hoặc hội viên",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<OrderResponseDTO> checkout(
            @Valid @RequestBody CheckoutRequestDTO request,
            Authentication authentication) {
        Long currentUserId = resolveStaffId(authentication);
        OrderResponseDTO response = posOrderService.createOrder(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/orders/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy chi tiết thành công",
                    content = @Content(schema = @Schema(implementation = OrderResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đơn hàng",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<OrderResponseDTO> getOrderById(
            @Parameter(description = "ID đơn hàng", example = "1")
            @PathVariable Long id) {
        OrderResponseDTO response = posOrderService.getOrderById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/orders/today")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrderResponseDTO.class)))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<List<OrderResponseDTO>> getTodayOrders() {
        List<OrderResponseDTO> list = posOrderService.getTodayOrders();
        return ResponseEntity.ok(list);
    }

    private Long resolveStaffId(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        try {
            String username = auth.getName();
            return staffRepository.findByEmailIgnoreCase(username)
                    .or(() -> staffRepository.findByCode(username))
                    .map(Staff::getId)
                    .orElse(null);
        } catch (Exception e) {
            log.warn("Không thể bóc tách staffId từ token: {}", e.getMessage());
            return null;
        }
    }
}
