package com.example.GYM_management_api.services;

import com.example.GYM_management_api.dtos.RevenueReportResponseDto;
import com.example.GYM_management_api.entities.enums.RevenueType;
import com.example.GYM_management_api.exceptions.BadRequestException;
import com.example.GYM_management_api.repositories.CheckInRepository;
import com.example.GYM_management_api.repositories.CheckInRepository.DailyCheckInProjection;
import com.example.GYM_management_api.repositories.TransactionRepository;
import com.example.GYM_management_api.repositories.TransactionRepository.DailyRevenueProjection;
import com.example.GYM_management_api.services.impl.ReportServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

        @Mock
        private TransactionRepository transactionRepository;

        @Mock
        private CheckInRepository checkInRepository;

        @InjectMocks
        private ReportServiceImpl reportService;

        // Helper implementation for DailyRevenueProjection mock
        private record TestDailyRevenueProjection(
                        String reportDate,
                        BigDecimal membershipRevenue,
                        BigDecimal retailRevenue,
                        BigDecimal totalRevenue,
                        Long transactionCount) implements DailyRevenueProjection {

                public TestDailyRevenueProjection(String reportDate, BigDecimal membershipRevenue, BigDecimal retailRevenue, BigDecimal totalRevenue) {
                        this(reportDate, membershipRevenue, retailRevenue, totalRevenue, 0L);
                }

                @Override
                public String getReportDate() {
                        return reportDate;
                }

                @Override
                public BigDecimal getMembershipRevenue() {
                        return membershipRevenue;
                }

                @Override
                public BigDecimal getRetailRevenue() {
                        return retailRevenue;
                }

                @Override
                public BigDecimal getTotalRevenue() {
                        return totalRevenue;
                }

                @Override
                public Long getTransactionCount() {
                        return transactionCount;
                }
        }

        // Helper implementation for DailyCheckInProjection mock
        private record TestDailyCheckInProjection(
                        String reportDate,
                        Long checkInCount) implements DailyCheckInProjection {
                @Override
                public String getReportDate() {
                        return reportDate;
                }

                @Override
                public Long getCheckInCount() {
                        return checkInCount;
                }
        }

        @Test
        @DisplayName("Ném lỗi 400 BadRequestException khi startDate lớn hơn endDate")
        void shouldThrowBadRequestException_whenStartDateAfterEndDate() {
                LocalDate start = LocalDate.of(2026, 10, 15);
                LocalDate end = LocalDate.of(2026, 10, 10);

                assertThatThrownBy(() -> reportService.getRevenueReport(start, end, RevenueType.ALL))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining("Ngày bắt đầu không được lớn hơn ngày kết thúc");
        }

        @Test
        @DisplayName("Ném lỗi 400 BadRequestException khi startDate hoặc endDate bị null")
        void shouldThrowBadRequestException_whenDateIsNull() {
                LocalDate start = LocalDate.of(2026, 10, 1);

                assertThatThrownBy(() -> reportService.getRevenueReport(null, start, RevenueType.ALL))
                                .isInstanceOf(BadRequestException.class);

                assertThatThrownBy(() -> reportService.getRevenueReport(start, null, RevenueType.ALL))
                                .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("Tổng hợp doanh thu bóc tách và check-in thành công với loại ALL (Gap filling các ngày không có data)")
        void shouldReturnCompleteReport_whenTypeIsAll() {
                LocalDate start = LocalDate.of(2026, 10, 1);
                LocalDate end = LocalDate.of(2026, 10, 3); // 3 ngày: 01, 02, 03

                // Ngày 01 có cả doanh thu gói tập và bán lẻ
                DailyRevenueProjection revDay1 = new TestDailyRevenueProjection(
                                "2026-10-01",
                                new BigDecimal("5000000.00"),
                                new BigDecimal("1500000.00"),
                                new BigDecimal("6500000.00"));
                // Ngày 03 chỉ có doanh thu bán lẻ
                DailyRevenueProjection revDay3 = new TestDailyRevenueProjection(
                                "2026-10-03",
                                BigDecimal.ZERO,
                                new BigDecimal("800000.00"),
                                new BigDecimal("800000.00"));

                when(transactionRepository.getDailyRevenueReport(any(), any()))
                                .thenReturn(List.of(revDay1, revDay3));

                // Check-in: ngày 01 có 25 lượt, ngày 02 có 10 lượt
                DailyCheckInProjection checkInDay1 = new TestDailyCheckInProjection("2026-10-01", 25L);
                DailyCheckInProjection checkInDay2 = new TestDailyCheckInProjection("2026-10-02", 10L);

                when(checkInRepository.getDailyCheckInReport(any(), any()))
                                .thenReturn(List.of(checkInDay1, checkInDay2));

                RevenueReportResponseDto result = reportService.getRevenueReport(start, end, RevenueType.ALL);

                assertThat(result).isNotNull();
                assertThat(result.getChartData()).hasSize(3);

                // Ngày 1: Có doanh thu và check-in
                var day1 = result.getChartData().get(0);
                assertThat(day1.getDate()).isEqualTo("2026-10-01");
                assertThat(day1.getMembershipRevenue()).isEqualTo(5000000.0);
                assertThat(day1.getRetailRevenue()).isEqualTo(1500000.0);
                assertThat(day1.getTotal()).isEqualTo(6500000.0);
                assertThat(day1.getCheckInCount()).isEqualTo(25L);

                // Ngày 2: Không có doanh thu (fill 0), có 10 check-in
                var day2 = result.getChartData().get(1);
                assertThat(day2.getDate()).isEqualTo("2026-10-02");
                assertThat(day2.getMembershipRevenue()).isEqualTo(0.0);
                assertThat(day2.getRetailRevenue()).isEqualTo(0.0);
                assertThat(day2.getTotal()).isEqualTo(0.0);
                assertThat(day2.getCheckInCount()).isEqualTo(10L);

                // Ngày 3: Có doanh thu bán lẻ, check-in fill 0
                var day3 = result.getChartData().get(2);
                assertThat(day3.getDate()).isEqualTo("2026-10-03");
                assertThat(day3.getMembershipRevenue()).isEqualTo(0.0);
                assertThat(day3.getRetailRevenue()).isEqualTo(800000.0);
                assertThat(day3.getTotal()).isEqualTo(800000.0);
                assertThat(day3.getCheckInCount()).isEqualTo(0L);

                // Summary tổng quan
                var summary = result.getSummary();
                assertThat(summary.getMembershipRevenue()).isEqualTo(5000000.0);
                assertThat(summary.getRetailRevenue()).isEqualTo(2300000.0);
                assertThat(summary.getTotalRevenue()).isEqualTo(7300000.0);
                assertThat(summary.getTotalCheckIns()).isEqualTo(35L);
        }

        @Test
        @DisplayName("Lọc theo loại MEMBERSHIP: tổng doanh thu chỉ tính từ gói tập, retail là 0")
        void shouldFilterCorrectly_whenTypeIsMembership() {
                LocalDate start = LocalDate.of(2026, 10, 1);
                LocalDate end = LocalDate.of(2026, 10, 1);

                DailyRevenueProjection revDay1 = new TestDailyRevenueProjection(
                                "2026-10-01",
                                new BigDecimal("4000000.00"),
                                new BigDecimal("1000000.00"),
                                new BigDecimal("5000000.00"));

                when(transactionRepository.getDailyRevenueReport(any(), any()))
                                .thenReturn(List.of(revDay1));
                when(checkInRepository.getDailyCheckInReport(any(), any()))
                                .thenReturn(Collections.emptyList());

                RevenueReportResponseDto result = reportService.getRevenueReport(start, end, RevenueType.MEMBERSHIP);

                var day1 = result.getChartData().get(0);
                assertThat(day1.getMembershipRevenue()).isEqualTo(4000000.0);
                assertThat(day1.getRetailRevenue()).isEqualTo(0.0);
                assertThat(day1.getTotal()).isEqualTo(4000000.0);

                assertThat(result.getSummary().getTotalRevenue()).isEqualTo(4000000.0);
                assertThat(result.getSummary().getRetailRevenue()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Lọc theo loại RETAIL: tổng doanh thu chỉ tính từ bán lẻ, membership là 0")
        void shouldFilterCorrectly_whenTypeIsRetail() {
                LocalDate start = LocalDate.of(2026, 10, 1);
                LocalDate end = LocalDate.of(2026, 10, 1);

                DailyRevenueProjection revDay1 = new TestDailyRevenueProjection(
                                "2026-10-01",
                                new BigDecimal("4000000.00"),
                                new BigDecimal("1000000.00"),
                                new BigDecimal("5000000.00"));

                when(transactionRepository.getDailyRevenueReport(any(), any()))
                                .thenReturn(List.of(revDay1));
                when(checkInRepository.getDailyCheckInReport(any(), any()))
                                .thenReturn(Collections.emptyList());

                RevenueReportResponseDto result = reportService.getRevenueReport(start, end, RevenueType.RETAIL);

                var day1 = result.getChartData().get(0);
                assertThat(day1.getMembershipRevenue()).isEqualTo(0.0);
                assertThat(day1.getRetailRevenue()).isEqualTo(1000000.0);
                assertThat(day1.getTotal()).isEqualTo(1000000.0);

                assertThat(result.getSummary().getTotalRevenue()).isEqualTo(1000000.0);
                assertThat(result.getSummary().getMembershipRevenue()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("CSDL trả về danh sách rỗng -> Báo cáo trả về tổng 0 đ và các ngày được lấp đầy bằng 0")
        void shouldReturnZeroRevenue_whenDatabaseReturnsEmptyList() {
                LocalDate start = LocalDate.of(2026, 10, 1);
                LocalDate end = LocalDate.of(2026, 10, 2); // 2 ngày

                when(transactionRepository.getDailyRevenueReport(any(), any()))
                                .thenReturn(Collections.emptyList());
                when(checkInRepository.getDailyCheckInReport(any(), any()))
                                .thenReturn(Collections.emptyList());

                RevenueReportResponseDto result = reportService.getRevenueReport(start, end, RevenueType.ALL);

                assertThat(result).isNotNull();
                assertThat(result.getChartData()).hasSize(2);

                // Kiểm tra dữ liệu từng ngày được fill 0
                for (var dayData : result.getChartData()) {
                        assertThat(dayData.getMembershipRevenue()).isEqualTo(0.0);
                        assertThat(dayData.getRetailRevenue()).isEqualTo(0.0);
                        assertThat(dayData.getTotal()).isEqualTo(0.0);
                        assertThat(dayData.getCheckInCount()).isEqualTo(0L);
                }

                // Summary trả về kết quả tổng 0 đ
                assertThat(result.getSummary().getTotalRevenue()).isEqualTo(0.0);
                assertThat(result.getSummary().getMembershipRevenue()).isEqualTo(0.0);
                assertThat(result.getSummary().getRetailRevenue()).isEqualTo(0.0);
                assertThat(result.getSummary().getTotalCheckIns()).isEqualTo(0L);
        }

        @Test
        @DisplayName("Tính toán công thức doanh thu: Tổng = Gói tập + Bán lẻ - Giảm giá voucher")
        void shouldCalculateRevenueFormulaCorrectly_MembershipPlusRetailMinusVoucherDiscount() {
                LocalDate start = LocalDate.of(2026, 10, 1);
                LocalDate end = LocalDate.of(2026, 10, 1);

                // Giả lập giao dịch:
                // Gói tập nguyên giá = 5,000,000, voucher giảm 500,000 -> thực thu = 4,500,000
                // Bán lẻ nguyên giá = 2,000,000, voucher giảm 200,000 -> thực thu = 1,800,000
                // Tổng doanh thu thực nhận = 4,500,000 + 1,800,000 = 6,300,000
                BigDecimal membershipGross = new BigDecimal("5000000.00");
                BigDecimal membershipDiscount = new BigDecimal("500000.00");
                BigDecimal membershipNet = membershipGross.subtract(membershipDiscount); // 4,500,000

                BigDecimal retailGross = new BigDecimal("2000000.00");
                BigDecimal retailDiscount = new BigDecimal("200000.00");
                BigDecimal retailNet = retailGross.subtract(retailDiscount); // 1,800,000

                BigDecimal totalExpected = membershipNet.add(retailNet); // 6,300,000

                DailyRevenueProjection revDay1 = new TestDailyRevenueProjection(
                                "2026-10-01",
                                membershipNet,
                                retailNet,
                                totalExpected);

                when(transactionRepository.getDailyRevenueReport(any(), any()))
                                .thenReturn(List.of(revDay1));
                when(checkInRepository.getDailyCheckInReport(any(), any()))
                                .thenReturn(Collections.emptyList());

                RevenueReportResponseDto result = reportService.getRevenueReport(start, end, RevenueType.ALL);

                assertThat(result).isNotNull();
                var summary = result.getSummary();
                assertThat(summary.getMembershipRevenue()).isEqualTo(4500000.0);
                assertThat(summary.getRetailRevenue()).isEqualTo(1800000.0);
                assertThat(summary.getTotalRevenue()).isEqualTo(6300000.0);

                // Khẳng định công thức: Tổng thực thu = Gói tập + Bán lẻ (đã trừ voucher giảm giá)
                double totalDiscount = membershipDiscount.add(retailDiscount).doubleValue();
                double grossRevenue = membershipGross.add(retailGross).doubleValue();
                assertThat(summary.getTotalRevenue()).isEqualTo(grossRevenue - totalDiscount);
        }
}
