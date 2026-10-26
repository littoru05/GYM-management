package com.example.GYM_management_api.services;

import com.example.GYM_management_api.dtos.VoucherValidateRequestDto;
import com.example.GYM_management_api.dtos.VoucherValidateResponseDto;
import com.example.GYM_management_api.entities.Membership;
import com.example.GYM_management_api.entities.Promotion;
import com.example.GYM_management_api.entities.enums.CommonStatus;
import com.example.GYM_management_api.entities.enums.DiscountType;
import com.example.GYM_management_api.exceptions.BadRequestException;
import com.example.GYM_management_api.exceptions.ResourceNotFoundException;
import com.example.GYM_management_api.repositories.MembershipRepository;
import com.example.GYM_management_api.repositories.PromotionRepository;
import com.example.GYM_management_api.services.impl.VoucherServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import com.example.GYM_management_api.utils.TestReportWatcher;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, TestReportWatcher.class})
class VoucherServiceImplTest {

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private MembershipRepository membershipRepository;

    @InjectMocks
    private VoucherServiceImpl voucherService;

    @Test
    @DisplayName("Validate voucher giảm giá 10% cho đơn 500.000 VNĐ thành công")
    void shouldValidatePercentageVoucherSuccessfully() {
        Promotion promo = Promotion.builder()
                .code("WELCOME10")
                .name("Chào mừng")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("10.00"))
                .startDate(LocalDate.now().minusDays(5))
                .endDate(LocalDate.now().plusDays(30))
                .usageLimit(100)
                .usedCount(5)
                .minOrderAmount(new BigDecimal("200000.00"))
                .maxDiscountAmount(new BigDecimal("100000.00"))
                .status(CommonStatus.ACTIVE)
                .applicableToAll(true)
                .build();

        when(promotionRepository.findByCodeIgnoreCase("WELCOME10")).thenReturn(Optional.of(promo));

        VoucherValidateRequestDto request = VoucherValidateRequestDto.builder()
                .code("WELCOME10")
                .orderAmount(new BigDecimal("500000.00"))
                .build();

        VoucherValidateResponseDto result = voucherService.validateVoucher(request);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getCode()).isEqualTo("WELCOME10");
        assertThat(result.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("50000.00"));
        assertThat(result.getFinalPrice()).isEqualByComparingTo(new BigDecimal("450000.00"));
        assertThat(result.getAmountAfterDiscount()).isEqualByComparingTo(new BigDecimal("450000.00"));
    }

    @Test
    @DisplayName("Validate voucher số tiền cố định 50.000 VNĐ thành công")
    void shouldValidateFixedVoucherSuccessfully() {
        Promotion promo = Promotion.builder()
                .code("SAVE50K")
                .name("Giảm 50K")
                .discountType(DiscountType.FIXED)
                .discountValue(new BigDecimal("50000.00"))
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(10))
                .usageLimit(50)
                .usedCount(0)
                .minOrderAmount(BigDecimal.ZERO)
                .status(CommonStatus.ACTIVE)
                .applicableToAll(true)
                .build();

        when(promotionRepository.findByCodeIgnoreCase("SAVE50K")).thenReturn(Optional.of(promo));

        VoucherValidateRequestDto request = VoucherValidateRequestDto.builder()
                .code("SAVE50K")
                .orderAmount(new BigDecimal("300000.00"))
                .build();

        VoucherValidateResponseDto result = voucherService.validateVoucher(request);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("50000.00"));
        assertThat(result.getFinalPrice()).isEqualByComparingTo(new BigDecimal("250000.00"));
    }

    @Test
    @DisplayName("Ném ngoại lệ khi mã voucher không tồn tại")
    void shouldThrowExceptionWhenVoucherNotFound() {
        when(promotionRepository.findByCodeIgnoreCase("NOTFOUND")).thenReturn(Optional.empty());

        VoucherValidateRequestDto request = VoucherValidateRequestDto.builder()
                .code("NOTFOUND")
                .orderAmount(new BigDecimal("500000.00"))
                .build();

        assertThatThrownBy(() -> voucherService.validateVoucher(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("không tồn tại");
    }

    @Test
    @DisplayName("Ném ngoại lệ khi mã voucher đã hết hạn")
    void shouldThrowExceptionWhenVoucherExpired() {
        Promotion promo = Promotion.builder()
                .code("EXPIRED")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("10.00"))
                .startDate(LocalDate.now().minusDays(30))
                .endDate(LocalDate.now().minusDays(1))
                .usageLimit(100)
                .usedCount(0)
                .minOrderAmount(BigDecimal.ZERO)
                .status(CommonStatus.ACTIVE)
                .build();

        when(promotionRepository.findByCodeIgnoreCase("EXPIRED")).thenReturn(Optional.of(promo));

        VoucherValidateRequestDto request = VoucherValidateRequestDto.builder()
                .code("EXPIRED")
                .orderAmount(new BigDecimal("500000.00"))
                .build();

        assertThatThrownBy(() -> voucherService.validateVoucher(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("đã hết hạn");
    }

    @Test
    @DisplayName("Ném ngoại lệ khi đơn hàng chưa đạt giá trị tối thiểu")
    void shouldThrowExceptionWhenOrderAmountBelowMinimum() {
        Promotion promo = Promotion.builder()
                .code("VIP100")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("20.00"))
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(10))
                .usageLimit(100)
                .usedCount(0)
                .minOrderAmount(new BigDecimal("1000000.00"))
                .status(CommonStatus.ACTIVE)
                .build();

        when(promotionRepository.findByCodeIgnoreCase("VIP100")).thenReturn(Optional.of(promo));

        VoucherValidateRequestDto request = VoucherValidateRequestDto.builder()
                .code("VIP100")
                .orderAmount(new BigDecimal("500000.00"))
                .build();

        assertThatThrownBy(() -> voucherService.validateVoucher(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("chưa đạt giá trị tối thiểu");
    }

    @Test
    @DisplayName("Ném ngoại lệ khi voucher không áp dụng cho gói tập được chỉ định")
    void shouldThrowExceptionWhenMembershipNotApplicable() {
        Promotion promo = Promotion.builder()
                .code("PACKAGE_ONLY")
                .discountType(DiscountType.FIXED)
                .discountValue(new BigDecimal("50000.00"))
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(10))
                .usageLimit(100)
                .usedCount(0)
                .minOrderAmount(BigDecimal.ZERO)
                .status(CommonStatus.ACTIVE)
                .applicableToAll(false)
                .applicableMembershipIds("2,3,4")
                .build();

        when(promotionRepository.findByCodeIgnoreCase("PACKAGE_ONLY")).thenReturn(Optional.of(promo));

        VoucherValidateRequestDto request = VoucherValidateRequestDto.builder()
                .code("PACKAGE_ONLY")
                .membershipId(1L)
                .orderAmount(new BigDecimal("500000.00"))
                .build();

        assertThatThrownBy(() -> voucherService.validateVoucher(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("không áp dụng cho gói tập đã chọn");
    }

    @Test
    @DisplayName("Ném ngoại lệ khi mã voucher vượt quá số lượt dùng tối đa (usedCount >= usageLimit)")
    void shouldThrowExceptionWhenVoucherUsageLimitReached() {
        Promotion promo = Promotion.builder()
                .code("MAX_USED")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("10.00"))
                .startDate(LocalDate.now().minusDays(5))
                .endDate(LocalDate.now().plusDays(10))
                .usageLimit(20)
                .usedCount(20)
                .minOrderAmount(BigDecimal.ZERO)
                .status(CommonStatus.ACTIVE)
                .applicableToAll(true)
                .build();

        when(promotionRepository.findByCodeIgnoreCase("MAX_USED")).thenReturn(Optional.of(promo));

        VoucherValidateRequestDto request = VoucherValidateRequestDto.builder()
                .code("MAX_USED")
                .orderAmount(new BigDecimal("500000.00"))
                .build();

        assertThatThrownBy(() -> voucherService.validateVoucher(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("đã hết lượt sử dụng");
    }

    @Test
    @DisplayName("Chặn trần mức giảm tối đa (maxDiscountAmount) khi số tiền giảm theo % vượt quá trần")
    void shouldCapDiscountAmountAtMaxDiscount_whenDiscountExceedsMax() {
        Promotion promo = Promotion.builder()
                .code("CAPPED_DISCOUNT")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("20.00")) // 20% của 1,000,000 là 200,000
                .maxDiscountAmount(new BigDecimal("80000.00")) // Trần là 80,000
                .startDate(LocalDate.now().minusDays(2))
                .endDate(LocalDate.now().plusDays(10))
                .usageLimit(100)
                .usedCount(2)
                .minOrderAmount(new BigDecimal("200000.00"))
                .status(CommonStatus.ACTIVE)
                .applicableToAll(true)
                .build();

        when(promotionRepository.findByCodeIgnoreCase("CAPPED_DISCOUNT")).thenReturn(Optional.of(promo));

        VoucherValidateRequestDto request = VoucherValidateRequestDto.builder()
                .code("CAPPED_DISCOUNT")
                .orderAmount(new BigDecimal("1000000.00"))
                .build();

        VoucherValidateResponseDto result = voucherService.validateVoucher(request);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("80000.00"));
        assertThat(result.getFinalPrice()).isEqualByComparingTo(new BigDecimal("920000.00"));
    }

    @Test
    @DisplayName("Ném ngoại lệ khi voucher chưa đến ngày bắt đầu áp dụng (today < startDate)")
    void shouldThrowExceptionWhenVoucherNotYetStarted() {
        Promotion promo = Promotion.builder()
                .code("UPCOMING")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("15.00"))
                .startDate(LocalDate.now().plusDays(2))
                .endDate(LocalDate.now().plusDays(20))
                .usageLimit(50)
                .usedCount(0)
                .minOrderAmount(BigDecimal.ZERO)
                .status(CommonStatus.ACTIVE)
                .applicableToAll(true)
                .build();

        when(promotionRepository.findByCodeIgnoreCase("UPCOMING")).thenReturn(Optional.of(promo));

        VoucherValidateRequestDto request = VoucherValidateRequestDto.builder()
                .code("UPCOMING")
                .orderAmount(new BigDecimal("300000.00"))
                .build();

        assertThatThrownBy(() -> voucherService.validateVoucher(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("chưa bắt đầu");
    }

    @Test
    @DisplayName("Ném ngoại lệ khi voucher bị tạm khóa hoặc đã ngưng áp dụng (status != ACTIVE)")
    void shouldThrowExceptionWhenVoucherInactive() {
        Promotion promo = Promotion.builder()
                .code("INACTIVE_CODE")
                .discountType(DiscountType.FIXED)
                .discountValue(new BigDecimal("30000.00"))
                .startDate(LocalDate.now().minusDays(5))
                .endDate(LocalDate.now().plusDays(10))
                .usageLimit(50)
                .usedCount(0)
                .minOrderAmount(BigDecimal.ZERO)
                .status(CommonStatus.INACTIVE)
                .applicableToAll(true)
                .build();

        when(promotionRepository.findByCodeIgnoreCase("INACTIVE_CODE")).thenReturn(Optional.of(promo));

        VoucherValidateRequestDto request = VoucherValidateRequestDto.builder()
                .code("INACTIVE_CODE")
                .orderAmount(new BigDecimal("300000.00"))
                .build();

        assertThatThrownBy(() -> voucherService.validateVoucher(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("tạm khóa hoặc đã ngưng áp dụng");
    }

    @Test
    @DisplayName("Mã voucher giảm số tiền cố định vượt giá trị đơn hàng -> Số tiền giảm tối đa bằng giá trị đơn hàng (finalPrice = 0)")
    void shouldCapDiscountAtOrderAmount_whenFixedDiscountExceedsOrderAmount() {
        Promotion promo = Promotion.builder()
                .code("FIXED_OVER")
                .discountType(DiscountType.FIXED)
                .discountValue(new BigDecimal("200000.00"))
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(10))
                .usageLimit(50)
                .usedCount(0)
                .minOrderAmount(BigDecimal.ZERO)
                .status(CommonStatus.ACTIVE)
                .applicableToAll(true)
                .build();

        when(promotionRepository.findByCodeIgnoreCase("FIXED_OVER")).thenReturn(Optional.of(promo));

        VoucherValidateRequestDto request = VoucherValidateRequestDto.builder()
                .code("FIXED_OVER")
                .orderAmount(new BigDecimal("150000.00"))
                .build();

        VoucherValidateResponseDto result = voucherService.validateVoucher(request);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("150000.00"));
        assertThat(result.getFinalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Ném ngoại lệ khi mã voucher bị để trống hoặc null")
    void shouldThrowExceptionWhenVoucherCodeIsBlankOrNull() {
        VoucherValidateRequestDto requestNull = VoucherValidateRequestDto.builder()
                .code(null)
                .orderAmount(new BigDecimal("200000.00"))
                .build();
        assertThatThrownBy(() -> voucherService.validateVoucher(requestNull))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Mã voucher không được để trống");

        VoucherValidateRequestDto requestBlank = VoucherValidateRequestDto.builder()
                .code("   ")
                .orderAmount(new BigDecimal("200000.00"))
                .build();
        assertThatThrownBy(() -> voucherService.validateVoucher(requestBlank))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Mã voucher không được để trống");
    }
}

