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

import com.example.GYM_management_api.entities.Transaction;
import com.example.GYM_management_api.entities.enums.TransactionType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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

    @Override
    public byte[] exportRevenueReportToExcel(LocalDate startDate, LocalDate endDate, RevenueType type) {
        RevenueReportResponseDto report = getRevenueReport(startDate, endDate, type);
        RevenueSummaryDto summary = report.getSummary();
        List<RevenueSeriesDto> chartData = report.getChartData();

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Styles
            DataFormat df = workbook.createDataFormat();
            short currencyFormat = df.getFormat("#,##0");
            short numberFormat = df.getFormat("#,##0");

            Font titleFont = workbook.createFont();
            titleFont.setFontName("Calibri");
            titleFont.setFontHeightInPoints((short) 16);
            titleFont.setBold(true);
            titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());

            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);

            Font subtitleFont = workbook.createFont();
            subtitleFont.setFontName("Calibri");
            subtitleFont.setFontHeightInPoints((short) 10);
            subtitleFont.setItalic(true);
            subtitleFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());

            CellStyle subtitleStyle = workbook.createCellStyle();
            subtitleStyle.setFont(subtitleFont);

            Font headerFont = workbook.createFont();
            headerFont.setFontName("Calibri");
            headerFont.setFontHeightInPoints((short) 11);
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);

            Font bodyFont = workbook.createFont();
            bodyFont.setFontName("Calibri");
            bodyFont.setFontHeightInPoints((short) 11);

            CellStyle dateCellStyle = workbook.createCellStyle();
            dateCellStyle.setFont(bodyFont);
            dateCellStyle.setAlignment(HorizontalAlignment.CENTER);
            dateCellStyle.setBorderBottom(BorderStyle.THIN);
            dateCellStyle.setBorderTop(BorderStyle.THIN);
            dateCellStyle.setBorderRight(BorderStyle.THIN);
            dateCellStyle.setBorderLeft(BorderStyle.THIN);

            CellStyle textCellStyle = workbook.createCellStyle();
            textCellStyle.setFont(bodyFont);
            textCellStyle.setAlignment(HorizontalAlignment.LEFT);
            textCellStyle.setBorderBottom(BorderStyle.THIN);
            textCellStyle.setBorderTop(BorderStyle.THIN);
            textCellStyle.setBorderRight(BorderStyle.THIN);
            textCellStyle.setBorderLeft(BorderStyle.THIN);

            CellStyle currencyCellStyle = workbook.createCellStyle();
            currencyCellStyle.setFont(bodyFont);
            currencyCellStyle.setDataFormat(currencyFormat);
            currencyCellStyle.setAlignment(HorizontalAlignment.RIGHT);
            currencyCellStyle.setBorderBottom(BorderStyle.THIN);
            currencyCellStyle.setBorderTop(BorderStyle.THIN);
            currencyCellStyle.setBorderRight(BorderStyle.THIN);
            currencyCellStyle.setBorderLeft(BorderStyle.THIN);

            CellStyle numberCellStyle = workbook.createCellStyle();
            numberCellStyle.setFont(bodyFont);
            numberCellStyle.setDataFormat(numberFormat);
            numberCellStyle.setAlignment(HorizontalAlignment.CENTER);
            numberCellStyle.setBorderBottom(BorderStyle.THIN);
            numberCellStyle.setBorderTop(BorderStyle.THIN);
            numberCellStyle.setBorderRight(BorderStyle.THIN);
            numberCellStyle.setBorderLeft(BorderStyle.THIN);

            Font totalFont = workbook.createFont();
            totalFont.setFontName("Calibri");
            totalFont.setFontHeightInPoints((short) 11);
            totalFont.setBold(true);

            CellStyle totalCurrencyStyle = workbook.createCellStyle();
            totalCurrencyStyle.setFont(totalFont);
            totalCurrencyStyle.setDataFormat(currencyFormat);
            totalCurrencyStyle.setAlignment(HorizontalAlignment.RIGHT);
            totalCurrencyStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            totalCurrencyStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            totalCurrencyStyle.setBorderTop(BorderStyle.DOUBLE);
            totalCurrencyStyle.setBorderBottom(BorderStyle.DOUBLE);
            totalCurrencyStyle.setBorderRight(BorderStyle.THIN);
            totalCurrencyStyle.setBorderLeft(BorderStyle.THIN);

            CellStyle totalLabelStyle = workbook.createCellStyle();
            totalLabelStyle.setFont(totalFont);
            totalLabelStyle.setAlignment(HorizontalAlignment.CENTER);
            totalLabelStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            totalLabelStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            totalLabelStyle.setBorderTop(BorderStyle.DOUBLE);
            totalLabelStyle.setBorderBottom(BorderStyle.DOUBLE);
            totalLabelStyle.setBorderRight(BorderStyle.THIN);
            totalLabelStyle.setBorderLeft(BorderStyle.THIN);

            CellStyle totalNumberStyle = workbook.createCellStyle();
            totalNumberStyle.setFont(totalFont);
            totalNumberStyle.setDataFormat(numberFormat);
            totalNumberStyle.setAlignment(HorizontalAlignment.CENTER);
            totalNumberStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            totalNumberStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            totalNumberStyle.setBorderTop(BorderStyle.DOUBLE);
            totalNumberStyle.setBorderBottom(BorderStyle.DOUBLE);
            totalNumberStyle.setBorderRight(BorderStyle.THIN);
            totalNumberStyle.setBorderLeft(BorderStyle.THIN);

            // ==========================================
            // SHEET 1: Báo cáo doanh thu theo ngày
            // ==========================================
            Sheet sheet1 = workbook.createSheet("Báo cáo doanh thu");
            sheet1.setDisplayGridlines(true);

            // Title
            Row titleRow = sheet1.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("BÁO CÁO DOANH THU & GIAO DỊCH PHÒNG TẬP");
            titleCell.setCellStyle(titleStyle);

            // Subtitle
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            Row subtitleRow = sheet1.createRow(1);
            Cell subtitleCell = subtitleRow.createCell(0);
            String typeStr = (type != null) ? type.name() : "ALL";
            subtitleCell.setCellValue(String.format("Khoảng thời gian: %s - %s | Loại doanh thu: %s | Thời điểm xuất: %s",
                    startDate.format(dtf), endDate.format(dtf), typeStr, LocalDateTime.now().format(timeFmt)));
            subtitleCell.setCellStyle(subtitleStyle);

            // Empty row 2
            // Headers at row 3
            Row headerRow = sheet1.createRow(3);
            headerRow.setHeightInPoints(26);
            String[] headers = {
                    "Ngày",
                    "Doanh thu hội viên (VNĐ)",
                    "Doanh thu bán lẻ (VNĐ)",
                    "Tổng doanh thu (VNĐ)",
                    "Số giao dịch",
                    "Số check-in"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 4;
            for (RevenueSeriesDto item : chartData) {
                Row row = sheet1.createRow(rowIdx++);
                row.setHeightInPoints(20);

                Cell c0 = row.createCell(0);
                c0.setCellValue(item.getDate());
                c0.setCellStyle(dateCellStyle);

                Cell c1 = row.createCell(1);
                c1.setCellValue(item.getMembershipRevenue());
                c1.setCellStyle(currencyCellStyle);

                Cell c2 = row.createCell(2);
                c2.setCellValue(item.getRetailRevenue());
                c2.setCellStyle(currencyCellStyle);

                Cell c3 = row.createCell(3);
                c3.setCellValue(item.getTotal());
                c3.setCellStyle(currencyCellStyle);

                Cell c4 = row.createCell(4);
                c4.setCellValue(item.getTransactionCount() != null ? item.getTransactionCount() : 0);
                c4.setCellStyle(numberCellStyle);

                Cell c5 = row.createCell(5);
                c5.setCellValue(item.getCheckInCount() != null ? item.getCheckInCount() : 0);
                c5.setCellStyle(numberCellStyle);
            }

            // Total row
            Row totalRow = sheet1.createRow(rowIdx);
            totalRow.setHeightInPoints(24);

            Cell t0 = totalRow.createCell(0);
            t0.setCellValue("TỔNG CỘNG");
            t0.setCellStyle(totalLabelStyle);

            Cell t1 = totalRow.createCell(1);
            t1.setCellValue(summary.getMembershipRevenue());
            t1.setCellStyle(totalCurrencyStyle);

            Cell t2 = totalRow.createCell(2);
            t2.setCellValue(summary.getRetailRevenue());
            t2.setCellStyle(totalCurrencyStyle);

            Cell t3 = totalRow.createCell(3);
            t3.setCellValue(summary.getTotalRevenue());
            t3.setCellStyle(totalCurrencyStyle);

            Cell t4 = totalRow.createCell(4);
            t4.setCellValue(summary.getTransactionCount() != null ? summary.getTransactionCount() : 0);
            t4.setCellStyle(totalNumberStyle);

            Cell t5 = totalRow.createCell(5);
            t5.setCellValue(summary.getTotalCheckIns() != null ? summary.getTotalCheckIns() : 0);
            t5.setCellStyle(totalNumberStyle);

            for (int i = 0; i < headers.length; i++) {
                sheet1.autoSizeColumn(i);
                sheet1.setColumnWidth(i, Math.max(sheet1.getColumnWidth(i) + 1200, 4200));
            }

            // ==========================================
            // SHEET 2: Danh sách giao dịch chi tiết
            // ==========================================
            Sheet sheet2 = workbook.createSheet("Danh sách giao dịch");
            sheet2.setDisplayGridlines(true);

            Row s2TitleRow = sheet2.createRow(0);
            Cell s2Title = s2TitleRow.createCell(0);
            s2Title.setCellValue("DANH SÁCH GIAO DỊCH CHI TIẾT");
            s2Title.setCellStyle(titleStyle);

            Row s2SubtitleRow = sheet2.createRow(1);
            Cell s2Sub = s2SubtitleRow.createCell(0);
            s2Sub.setCellValue(String.format("Từ ngày %s đến ngày %s", startDate.format(dtf), endDate.format(dtf)));
            s2Sub.setCellStyle(subtitleStyle);

            Row s2HeaderRow = sheet2.createRow(3);
            s2HeaderRow.setHeightInPoints(26);
            String[] s2Headers = {
                    "STT", "Mã GD", "Thời gian", "Khách hàng", "Phân loại", "Hình thức",
                    "Giá gốc (VNĐ)", "Giảm giá (VNĐ)", "Thực thu (VNĐ)", "Mã voucher", "Nhân viên", "Nội dung"
            };

            for (int i = 0; i < s2Headers.length; i++) {
                Cell cell = s2HeaderRow.createCell(i);
                cell.setCellValue(s2Headers[i]);
                cell.setCellStyle(headerStyle);
            }

            LocalDateTime startDt = startDate.atStartOfDay();
            LocalDateTime endDt = endDate.atTime(LocalTime.MAX);
            List<Transaction> transactions;
            if (type == RevenueType.MEMBERSHIP) {
                transactions = transactionRepository.findByTypeAndTransactionTimeBetweenOrderByTransactionTimeDesc(
                        TransactionType.MEMBERSHIP, startDt, endDt);
            } else if (type == RevenueType.RETAIL) {
                transactions = transactionRepository.findByTypeAndTransactionTimeBetweenOrderByTransactionTimeDesc(
                        TransactionType.RETAIL, startDt, endDt);
            } else {
                transactions = transactionRepository.findByTransactionTimeBetweenOrderByTransactionTimeDesc(startDt, endDt);
            }

            int txIdx = 4;
            int stt = 1;
            for (Transaction tx : transactions) {
                Row row = sheet2.createRow(txIdx++);
                row.setHeightInPoints(20);

                Cell cStt = row.createCell(0);
                cStt.setCellValue(stt++);
                cStt.setCellStyle(numberCellStyle);

                Cell cCode = row.createCell(1);
                cCode.setCellValue(tx.getCode());
                cCode.setCellStyle(textCellStyle);

                Cell cTime = row.createCell(2);
                cTime.setCellValue(tx.getTransactionTime() != null ? tx.getTransactionTime().format(timeFmt) : "");
                cTime.setCellStyle(dateCellStyle);

                Cell cMember = row.createCell(3);
                cMember.setCellValue(tx.getMemberName() != null ? tx.getMemberName() : "Khách vãng lai");
                cMember.setCellStyle(textCellStyle);

                Cell cType = row.createCell(4);
                cType.setCellValue(tx.getType() == TransactionType.MEMBERSHIP ? "Gói tập" : "Bán lẻ");
                cType.setCellStyle(dateCellStyle);

                Cell cMethod = row.createCell(5);
                cMethod.setCellValue(tx.getPaymentMethod() != null ? tx.getPaymentMethod().name() : "CASH");
                cMethod.setCellStyle(dateCellStyle);

                Cell cSubtotal = row.createCell(6);
                cSubtotal.setCellValue(tx.getSubtotal() != null ? tx.getSubtotal().doubleValue() : tx.getAmount().doubleValue());
                cSubtotal.setCellStyle(currencyCellStyle);

                Cell cDiscount = row.createCell(7);
                cDiscount.setCellValue(tx.getDiscountAmount() != null ? tx.getDiscountAmount().doubleValue() : 0.0);
                cDiscount.setCellStyle(currencyCellStyle);

                Cell cAmount = row.createCell(8);
                cAmount.setCellValue(tx.getAmount() != null ? tx.getAmount().doubleValue() : 0.0);
                cAmount.setCellStyle(currencyCellStyle);

                Cell cVoucher = row.createCell(9);
                cVoucher.setCellValue(tx.getPromotion() != null ? tx.getPromotion().getCode() : "—");
                cVoucher.setCellStyle(dateCellStyle);

                Cell cStaff = row.createCell(10);
                cStaff.setCellValue(tx.getStaffName() != null ? tx.getStaffName() : "Quầy lễ tân");
                cStaff.setCellStyle(textCellStyle);

                Cell cDesc = row.createCell(11);
                cDesc.setCellValue(tx.getDescription() != null ? tx.getDescription() : "");
                cDesc.setCellStyle(textCellStyle);
            }

            for (int i = 0; i < s2Headers.length; i++) {
                sheet2.autoSizeColumn(i);
                sheet2.setColumnWidth(i, Math.max(sheet2.getColumnWidth(i) + 1200, 3500));
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            log.error("Lỗi khi tạo file Excel báo cáo doanh thu", e);
            throw new RuntimeException("Không thể xuất file Excel báo cáo: " + e.getMessage(), e);
        }
    }
}
