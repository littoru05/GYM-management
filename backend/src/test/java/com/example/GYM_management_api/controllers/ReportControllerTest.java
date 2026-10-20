package com.example.GYM_management_api.controllers;

import com.example.GYM_management_api.dtos.RevenueReportResponseDto;
import com.example.GYM_management_api.dtos.RevenueSeriesDto;
import com.example.GYM_management_api.dtos.RevenueSummaryDto;
import com.example.GYM_management_api.entities.enums.RevenueType;
import com.example.GYM_management_api.exceptions.BadRequestException;
import com.example.GYM_management_api.exceptions.GlobalExceptionHandler;
import com.example.GYM_management_api.security.JwtRequestFilter;
import com.example.GYM_management_api.services.IReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ReportControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private IReportService reportService;

        @MockitoBean
        private JwtRequestFilter jwtRequestFilter;

        @Test
        @DisplayName("GET /api/admin/reports/revenue - Thành công (200 OK) trả về đúng cấu trúc biểu đồ")
        void shouldReturn200AndChartJson_whenValidParams() throws Exception {
                RevenueSeriesDto day1 = RevenueSeriesDto.builder()
                                .date("2026-10-01")
                                .membershipRevenue(5000000.0)
                                .retailRevenue(1500000.0)
                                .total(6500000.0)
                                .checkInCount(25L)
                                .build();

                RevenueSummaryDto summary = RevenueSummaryDto.builder()
                                .totalRevenue(6500000.0)
                                .membershipRevenue(5000000.0)
                                .retailRevenue(1500000.0)
                                .totalCheckIns(25L)
                                .build();

                RevenueReportResponseDto mockResponse = RevenueReportResponseDto.builder()
                                .summary(summary)
                                .chartData(List.of(day1))
                                .build();

                when(reportService.getRevenueReport(eq(LocalDate.of(2026, 10, 1)), eq(LocalDate.of(2026, 10, 31)),
                                eq(RevenueType.ALL)))
                                .thenReturn(mockResponse);

                mockMvc.perform(get("/api/admin/reports/revenue")
                                .param("startDate", "2026-10-01")
                                .param("endDate", "2026-10-31")
                                .param("type", "ALL")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.summary.totalRevenue").value(6500000.0))
                                .andExpect(jsonPath("$.summary.membershipRevenue").value(5000000.0))
                                .andExpect(jsonPath("$.summary.retailRevenue").value(1500000.0))
                                .andExpect(jsonPath("$.summary.totalCheckIns").value(25))
                                .andExpect(jsonPath("$.chartData[0].date").value("2026-10-01"))
                                .andExpect(jsonPath("$.chartData[0].membershipRevenue").value(5000000.0))
                                .andExpect(jsonPath("$.chartData[0].retailRevenue").value(1500000.0))
                                .andExpect(jsonPath("$.chartData[0].total").value(6500000.0))
                                .andExpect(jsonPath("$.chartData[0].totalRevenue").value(6500000.0))
                                .andExpect(jsonPath("$.chartData[0].checkInCount").value(25));
        }

        @Test
        @DisplayName("GET /api/admin/reports/revenue - Ném mã lỗi 400 Bad Request nếu startDate > endDate")
        void shouldReturn400BadRequest_whenStartDateAfterEndDate() throws Exception {
                when(reportService.getRevenueReport(any(), any(), any()))
                                .thenThrow(new BadRequestException("Ngày bắt đầu không được lớn hơn ngày kết thúc."));

                mockMvc.perform(get("/api/admin/reports/revenue")
                                .param("startDate", "2026-10-31")
                                .param("endDate", "2026-10-01")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.error").value("Bad Request"))
                                .andExpect(jsonPath("$.message")
                                                .value("Ngày bắt đầu không được lớn hơn ngày kết thúc."));
        }

        @Test
        @DisplayName("GET /api/admin/reports/revenue - Ném mã lỗi 400 Bad Request khi truyền type không hợp lệ")
        void shouldReturn400BadRequest_whenInvalidType() throws Exception {
                mockMvc.perform(get("/api/admin/reports/revenue")
                                .param("startDate", "2026-10-01")
                                .param("endDate", "2026-10-31")
                                .param("type", "INVALID_TYPE")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.message").value(
                                                "Loại doanh thu không hợp lệ: 'INVALID_TYPE'. Các giá trị hợp lệ: ALL, MEMBERSHIP, RETAIL"));
        }

        @Test
        @DisplayName("GET /api/admin/reports/revenue - Thành công trả về tổng 0 đ khi khoảng thời gian không có doanh thu")
        void shouldReturn200WithZeroRevenue_whenNoTransactions() throws Exception {
                RevenueSummaryDto summary = RevenueSummaryDto.builder()
                                .totalRevenue(0.0)
                                .membershipRevenue(0.0)
                                .retailRevenue(0.0)
                                .totalCheckIns(0L)
                                .build();

                RevenueReportResponseDto mockResponse = RevenueReportResponseDto.builder()
                                .summary(summary)
                                .chartData(List.of())
                                .build();

                when(reportService.getRevenueReport(eq(LocalDate.of(2026, 11, 1)), eq(LocalDate.of(2026, 11, 2)), eq(RevenueType.ALL)))
                                .thenReturn(mockResponse);

                mockMvc.perform(get("/api/admin/reports/revenue")
                                .param("startDate", "2026-11-01")
                                .param("endDate", "2026-11-02")
                                .param("type", "ALL")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.summary.totalRevenue").value(0.0))
                                .andExpect(jsonPath("$.summary.membershipRevenue").value(0.0))
                                .andExpect(jsonPath("$.summary.retailRevenue").value(0.0))
                                .andExpect(jsonPath("$.summary.totalCheckIns").value(0))
                                .andExpect(jsonPath("$.chartData").isEmpty());
        }

        @Test
        @DisplayName("GET /api/admin/reports/revenue - Ném 400 Bad Request khi thiếu tham số endDate")
        void shouldReturn400BadRequest_whenMissingEndDate() throws Exception {
                mockMvc.perform(get("/api/admin/reports/revenue")
                                .param("startDate", "2026-10-01")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.error").value("Bad Request"))
                                .andExpect(jsonPath("$.message", containsString("endDate")));
        }

        @Test
        @DisplayName("GET /api/admin/reports/revenue - Ném 400 Bad Request khi thiếu tham số startDate")
        void shouldReturn400BadRequest_whenMissingStartDate() throws Exception {
                mockMvc.perform(get("/api/admin/reports/revenue")
                                .param("endDate", "2026-10-31")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.error").value("Bad Request"))
                                .andExpect(jsonPath("$.message", containsString("startDate")));
        }

        @Test
        @DisplayName("GET /api/admin/reports/revenue - Ném 400 Bad Request khi thiếu cả hai tham số ngày")
        void shouldReturn400BadRequest_whenMissingBothDateParams() throws Exception {
                mockMvc.perform(get("/api/admin/reports/revenue")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.error").value("Bad Request"));
        }
}
