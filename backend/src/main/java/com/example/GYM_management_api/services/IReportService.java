package com.example.GYM_management_api.services;

import com.example.GYM_management_api.dtos.RevenueReportResponseDto;
import com.example.GYM_management_api.entities.enums.RevenueType;

import java.time.LocalDate;

public interface IReportService {

    /**
     * Báo cáo tổng hợp số liệu doanh thu và lượt check-in theo khoảng thời gian
     *
     * @param startDate Ngày bắt đầu (bắt buộc)
     * @param endDate   Ngày kết thúc (bắt buộc)
     * @param type      Loại doanh thu (ALL, MEMBERSHIP, RETAIL)
     * @return Dữ liệu tổng quan và chuỗi dữ liệu theo ngày cho biểu đồ
     */
    RevenueReportResponseDto getRevenueReport(LocalDate startDate, LocalDate endDate, RevenueType type);
}
