package com.example.GYM_management_api.services.impl;

import com.example.GYM_management_api.dtos.RevenueReportResponseDto;
import com.example.GYM_management_api.dtos.RevenueSeriesDto;
import com.example.GYM_management_api.dtos.RevenueSummaryDto;
import com.example.GYM_management_api.entities.enums.RevenueType;
import com.example.GYM_management_api.exceptions.BadRequestException;
import com.example.GYM_management_api.repositories.CheckInRepository;
import com.example.GYM_management_api.repositories.CheckInRepository.DailyCheckInProjection;
import com.example.GYM_management_api.repositories.TransactionRepository;
import com.example.GYM_management_api.repositories.TransactionRepository.DailyRevenueProjection;
import com.example.GYM_management_api.services.IReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ReportServiceImpl implements IReportService {

    private final TransactionRepository transactionRepository;
    private final CheckInRepository checkInRepository;

    @Override
    public RevenueReportResponseDto getRevenueReport(LocalDate startDate, LocalDate endDate, RevenueType type) {
        if (startDate == null || endDate == null) {
            throw new BadRequestException("Ngày bắt đầu và ngày kết thúc không được để trống.");
        }

        if (startDate.isAfter(endDate)) {
            log.warn("Lỗi truy vấn báo cáo: startDate ({}) lớn hơn endDate ({})", startDate, endDate);
            throw new BadRequestException("Ngày bắt đầu không được lớn hơn ngày kết thúc.");
        }

        RevenueType resolvedType = (type != null) ? type : RevenueType.ALL;

        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.atTime(LocalTime.MAX);

        log.debug("Truy vấn báo cáo doanh thu từ {} đến {}, loại: {}", startTime, endTime, resolvedType);

        // 1. Lấy dữ liệu doanh thu bóc tách từ bảng transactions
        List<DailyRevenueProjection> revenueProjections = transactionRepository.getDailyRevenueReport(startTime, endTime);
        Map<String, DailyRevenueProjection> revenueMap = revenueProjections.stream()
                .filter(p -> p.getReportDate() != null)
                .collect(Collectors.toMap(DailyRevenueProjection::getReportDate, p -> p, (a, b) -> a));

        // 2. Lấy dữ liệu check-in thành công từ bảng check_ins (UC08)
        List<DailyCheckInProjection> checkInProjections = checkInRepository.getDailyCheckInReport(startTime, endTime);
        Map<String, Long> checkInMap = checkInProjections.stream()
                .filter(p -> p.getReportDate() != null)
                .collect(Collectors.toMap(
                        DailyCheckInProjection::getReportDate,
                        p -> p.getCheckInCount() != null ? p.getCheckInCount() : 0L,
                        (a, b) -> a
                ));

        // 3. Khởi tạo danh sách chuỗi thời gian liên tục từ startDate đến endDate (Gap Filling với 0)
        List<RevenueSeriesDto> chartData = new ArrayList<>();
        double totalRevenueSum = 0.0;
        double membershipRevenueSum = 0.0;
        double retailRevenueSum = 0.0;
        long totalCheckInsSum = 0L;
        long totalTransactionsSum = 0L;
        int todayTransactionsCount = 0;
        LocalDate today = LocalDate.now();

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            String dateStr = current.toString();
            DailyRevenueProjection rev = revenueMap.get(dateStr);
            Long checkIns = checkInMap.getOrDefault(dateStr, 0L);

            double rawMembership = (rev != null && rev.getMembershipRevenue() != null)
                    ? rev.getMembershipRevenue().doubleValue()
                    : 0.0;
            double rawRetail = (rev != null && rev.getRetailRevenue() != null)
                    ? rev.getRetailRevenue().doubleValue()
                    : 0.0;

            double membership;
            double retail;
            double dailyTotal;

            if (resolvedType == RevenueType.MEMBERSHIP) {
                membership = rawMembership;
                retail = 0.0;
                dailyTotal = membership;
            } else if (resolvedType == RevenueType.RETAIL) {
                membership = 0.0;
                retail = rawRetail;
                dailyTotal = retail;
            } else {
                membership = rawMembership;
                retail = rawRetail;
                dailyTotal = rawMembership + rawRetail;
            }

            long dailyTxCount = (rev != null && rev.getTransactionCount() != null)
                    ? rev.getTransactionCount()
                    : 0L;
            totalTransactionsSum += dailyTxCount;
            if (current.equals(today)) {
                todayTransactionsCount = (int) dailyTxCount;
            }

            chartData.add(RevenueSeriesDto.builder()
                    .date(dateStr)
                    .membershipRevenue(membership)
                    .retailRevenue(retail)
                    .total(dailyTotal)
                    .checkInCount(checkIns)
                    .transactionCount(dailyTxCount)
                    .build());

            totalRevenueSum += dailyTotal;
            membershipRevenueSum += membership;
            retailRevenueSum += retail;
            totalCheckInsSum += checkIns;

            current = current.plusDays(1);
        }

        RevenueSummaryDto summary = RevenueSummaryDto.builder()
                .totalRevenue(totalRevenueSum)
                .membershipRevenue(membershipRevenueSum)
                .retailRevenue(retailRevenueSum)
                .totalCheckIns(totalCheckInsSum)
                .transactionCount(totalTransactionsSum)
                .todayTransactions(todayTransactionsCount)
                .build();

        return RevenueReportResponseDto.builder()
                .summary(summary)
                .chartData(chartData)
                .build();
    }
}
