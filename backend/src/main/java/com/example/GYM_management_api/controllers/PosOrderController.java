package com.example.GYM_management_api.controllers;

import com.example.GYM_management_api.dtos.CheckoutRequestDTO;
import com.example.GYM_management_api.dtos.ErrorResponseDto;
import com.example.GYM_management_api.dtos.OrderResponseDTO;
import com.example.GYM_management_api.entities.Staff;
import com.example.GYM_management_api.repositories.StaffRepository;
import com.example.GYM_management_api.services.IPosOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
    @Operation(summary = "Tạo đơn hàng bán lẻ Mini POS (Checkout)")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Tạo đơn hàng thành công, trừ kho sản phẩm chính xác",
                    content = @Content(
                            schema = @Schema(implementation = OrderResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Thành công",
                                    value = """
                                    {
                                      "id": 1,
                                      "code": "POS202610061234",
                                      "orderDate": "2026-10-06T15:30:00",
                                      "transactionTime": "2026-10-06T15:30:00",
                                      "staffId": 2,
                                      "staffName": "Nguyễn Văn Thu Ngân",
                                      "memberId": 1,
                                      "memberName": "Trần Khách Lẻ",
                                      "paymentMethod": "CASH",
                                      "subtotal": 50000.00,
                                      "discountAmount": 0.00,
                                      "totalAmount": 50000.00,
                                      "notes": "Bán lẻ nước suối tại quầy",
                                      "status": "COMPLETED",
                                      "items": [
                                        {
                                          "id": 1,
                                          "productId": 1,
                                          "productSku": "DRK001",
                                          "productName": "Nước khoáng Lavie 500ml",
                                          "quantity": 2,
                                          "unitPrice": 15000.00,
                                          "unitCost": 8000.00,
                                          "totalPrice": 30000.00
                                        },
                                        {
                                          "id": 2,
                                          "productId": 2,
                                          "productSku": "SNK001",
                                          "productName": "Thanh Protein Bar",
                                          "quantity": 1,
                                          "unitPrice": 20000.00,
                                          "unitCost": 12000.00,
                                          "totalPrice": 20000.00
                                        }
                                      ]
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dữ liệu không hợp lệ hoặc thiếu hàng tồn kho (Tự động Rollback, không sinh đơn rác)",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(
                                    name = "Hết hàng",
                                    value = """
                                    {
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Sản phẩm 'Nước tăng lực Redbull' không đủ số lượng tồn kho (Tồn: 2, Yêu cầu: 5).",
                                      "timestamp": "2026-10-06T15:30:00",
                                      "path": "/api/pos/checkout"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập / Token không hợp lệ",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập (Chỉ ADMIN, STAFF)",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy sản phẩm hoặc hội viên",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<OrderResponseDTO> checkout(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            schema = @Schema(implementation = CheckoutRequestDTO.class),
                            examples = @ExampleObject(
                                    name = "Đơn thanh toán mẫu",
                                    value = """
                                    {
                                      "items": [
                                        {
                                          "productId": 1,
                                          "quantity": 2
                                        }
                                      ],
                                      "paymentMethod": "CASH",
                                      "memberId": 1,
                                      "discountAmount": 0,
                                      "notes": "Khách thanh toán tại quầy POS"
                                    }
                                    """
                            )
                    )
            )
            @Valid @RequestBody CheckoutRequestDTO request,
            Authentication authentication) {
        Long currentUserId = resolveStaffId(authentication);
        OrderResponseDTO response = posOrderService.createOrder(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/orders/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Xem chi tiết đơn hàng theo ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy chi tiết đơn hàng thành công",
                    content = @Content(schema = @Schema(implementation = OrderResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đơn hàng",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<OrderResponseDTO> getOrderById(
            @Parameter(description = "ID của đơn hàng", example = "1")
            @PathVariable Long id) {
        OrderResponseDTO response = posOrderService.getOrderById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/orders/today")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Danh sách đơn hàng phát sinh hôm nay")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách đơn hàng hôm nay thành công",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrderResponseDTO.class))))
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
