package com.example.GYM_management_api.controllers;

import com.example.GYM_management_api.dtos.ErrorResponseDto;
import com.example.GYM_management_api.dtos.RevenueReportResponseDto;
import com.example.GYM_management_api.entities.enums.RevenueType;
import com.example.GYM_management_api.services.IReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Báo cáo & Thống kê Quản trị (Admin Reports)")
@SecurityRequirement(name = "Bearer Authentication")
public class ReportController {

    private final IReportService reportService;

    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Báo cáo tổng hợp số liệu doanh thu và lượt check-in",
            description = "Lấy dữ liệu doanh thu bóc tách 3 nguồn (Tổng doanh thu, Gói tập UC07, Bán lẻ POS UC09) " +
                    "và số lượt check-in theo ngày từ lịch sử UC08. Hỗ trợ lọc theo loại doanh thu (ALL, MEMBERSHIP, RETAIL)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lấy dữ liệu báo cáo thành công",
                    content = @Content(schema = @Schema(implementation = RevenueReportResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Yêu cầu không hợp lệ (startDate > endDate hoặc tham số không đúng)",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Chưa xác thực hoặc token hết hạn",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Từ chối quyền truy cập (Yêu cầu quyền ADMIN)",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })
    public ResponseEntity<RevenueReportResponseDto> getRevenueReport(
            @Parameter(description = "Ngày bắt đầu khoảng thời gian thống kê (YYYY-MM-DD) - Bắt buộc", example = "2026-10-01", required = false)
            @RequestParam(name = "startDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @Parameter(description = "Ngày kết thúc khoảng thời gian thống kê (YYYY-MM-DD) - Bắt buộc", example = "2026-10-31", required = false)
            @RequestParam(name = "endDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @Parameter(description = "Loại doanh thu cần lọc: ALL (Tất cả), MEMBERSHIP (Gói tập), RETAIL (Bán lẻ quầy)", example = "ALL")
            @RequestParam(name = "type", required = false, defaultValue = "ALL")
            String type
    ) {
        RevenueType revenueType = RevenueType.fromString(type);
        RevenueReportResponseDto response = reportService.getRevenueReport(startDate, endDate, revenueType);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/revenue/export")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Xuất file Excel báo cáo doanh thu và giao dịch (.xlsx)",
            description = "Xuất toàn bộ dữ liệu thống kê doanh thu theo ngày và danh sách giao dịch chi tiết ra file Excel định dạng .xlsx"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Xuất file Excel thành công"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<byte[]> exportRevenueReportToExcel(
            @Parameter(description = "Ngày bắt đầu khoảng thời gian thống kê (YYYY-MM-DD)", example = "2026-10-01")
            @RequestParam(name = "startDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @Parameter(description = "Ngày kết thúc khoảng thời gian thống kê (YYYY-MM-DD)", example = "2026-10-31")
            @RequestParam(name = "endDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @Parameter(description = "Loại doanh thu cần lọc: ALL (Tất cả), MEMBERSHIP (Gói tập), RETAIL (Bán lẻ quầy)", example = "ALL")
            @RequestParam(name = "type", required = false, defaultValue = "ALL")
            String type
    ) {
        RevenueType revenueType = RevenueType.fromString(type);
        byte[] excelBytes = reportService.exportRevenueReportToExcel(startDate, endDate, revenueType);

        String filename = String.format("revenue-report_%s_%s.xlsx", startDate, endDate);

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(org.springframework.http.HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, org.springframework.http.HttpHeaders.CONTENT_DISPOSITION)
                .contentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }
}
