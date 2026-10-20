package com.example.GYM_management_api.controllers;

import com.example.GYM_management_api.dtos.ErrorResponseDto;
import com.example.GYM_management_api.dtos.StaffDto;
import com.example.GYM_management_api.services.IStaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/staffs")
@RequiredArgsConstructor
@Tag(name = "Quản trị - Quản lý Tài khoản Nhân viên")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
public class StaffController {

        private final IStaffService staffService;

        @GetMapping
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
                        @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
                        @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
        })
        public ResponseEntity<Page<StaffDto>> getStaffs(
                        @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Số lượng phần tử mỗi trang", example = "10") @RequestParam(defaultValue = "10") int size,
                        @Parameter(description = "Từ khóa tìm kiếm (tên, mã, email, SĐT)", example = "Nguyễn") @RequestParam(required = false) String keyword,
                        @Parameter(description = "Lọc theo vai trò (ADMIN, STAFF)", example = "STAFF") @RequestParam(required = false) String role,
                        @Parameter(description = "Lọc theo trạng thái (ACTIVE, LOCKED, INACTIVE)", example = "ACTIVE") @RequestParam(required = false) String status) {
                Page<StaffDto> result = staffService.getStaffs(page, size, keyword, role, status);
                return ResponseEntity.ok(result);
        }

        @GetMapping("/{id}")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy nhân viên", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
        })
        public ResponseEntity<StaffDto> getStaffById(
                        @Parameter(description = "ID của nhân viên", example = "1") @PathVariable Long id) {
                StaffDto dto = staffService.getStaffById(id);
                return ResponseEntity.ok(dto);
        }

        @PostMapping
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Tạo tài khoản thành công"),
                        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
                        @ApiResponse(responseCode = "409", description = "Trùng lặp email, SĐT hoặc mã nhân viên", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
        })
        public ResponseEntity<StaffDto> createStaff(
                        @Valid @RequestBody StaffDto dto) {
                StaffDto created = staffService.createStaff(dto);
                return ResponseEntity.status(HttpStatus.CREATED).body(created);
        }

        @PutMapping("/{id}")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
                        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy nhân viên", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
                        @ApiResponse(responseCode = "409", description = "Trùng lặp email hoặc SĐT với tài khoản khác", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
        })
        public ResponseEntity<StaffDto> updateStaff(
                        @Parameter(description = "ID của nhân viên", example = "1") @PathVariable Long id,
                        @Valid @RequestBody StaffDto dto,
                        Authentication authentication) {
                if (authentication == null) {
                        authentication = org.springframework.security.core.context.SecurityContextHolder.getContext()
                                        .getAuthentication();
                }
                String currentAdminEmail = authentication != null ? authentication.getName() : null;
                StaffDto updated = staffService.updateStaff(id, dto, currentAdminEmail);
                return ResponseEntity.ok(updated);
        }

        @PatchMapping("/{id}/toggle-status")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Chuyển đổi trạng thái thành công"),
                        @ApiResponse(responseCode = "400", description = "Không thể tự khóa tài khoản Admin đang đăng nhập", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy nhân viên", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
        })
        public ResponseEntity<StaffDto> toggleStaffStatus(
                        @Parameter(description = "ID của nhân viên", example = "1") @PathVariable Long id,
                        Authentication authentication) {
                if (authentication == null) {
                        authentication = org.springframework.security.core.context.SecurityContextHolder.getContext()
                                        .getAuthentication();
                }
                String currentAdminEmail = authentication != null ? authentication.getName() : null;
                StaffDto result = staffService.toggleStaffStatus(id, currentAdminEmail);
                return ResponseEntity.ok(result);
        }

        @DeleteMapping("/{id}")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Vô hiệu hóa thành công"),
                        @ApiResponse(responseCode = "400", description = "Không thể tự xóa tài khoản của chính mình", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy nhân viên", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
        })
        public ResponseEntity<Map<String, Object>> deleteStaff(
                        @Parameter(description = "ID của nhân viên", example = "1") @PathVariable Long id,
                        Authentication authentication) {
                if (authentication == null) {
                        authentication = org.springframework.security.core.context.SecurityContextHolder.getContext()
                                        .getAuthentication();
                }
                String currentAdminEmail = authentication != null ? authentication.getName() : null;
                staffService.deleteStaff(id, currentAdminEmail);
                return ResponseEntity.ok(Map.of(
                                "status", HttpStatus.OK.value(),
                                "message", "Vô hiệu hóa tài khoản nhân viên thành công."));
        }
}
