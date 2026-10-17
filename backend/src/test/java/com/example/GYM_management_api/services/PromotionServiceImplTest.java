package com.example.GYM_management_api.services;

import com.example.GYM_management_api.dtos.PromotionDto;
import com.example.GYM_management_api.dtos.VoucherApplyRequestDto;
import com.example.GYM_management_api.dtos.VoucherApplyResponseDto;
import com.example.GYM_management_api.entities.Promotion;
import com.example.GYM_management_api.entities.enums.CommonStatus;
import com.example.GYM_management_api.entities.enums.DiscountType;
import com.example.GYM_management_api.exceptions.BadRequestException;
import com.example.GYM_management_api.exceptions.DuplicateResourceException;
import com.example.GYM_management_api.exceptions.ResourceNotFoundException;
import com.example.GYM_management_api.mappers.PromotionMapper;
import com.example.GYM_management_api.repositories.PromotionRepository;
import com.example.GYM_management_api.services.impl.PromotionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromotionServiceImplTest {

    @Mock
    private PromotionRepository promotionRepository;

    @Spy
    private PromotionMapper promotionMapper;

    @InjectMocks
    private PromotionServiceImpl promotionService;

    private Promotion samplePercentagePromo;
    private Promotion sampleFixedPromo;

    @BeforeEach
    void setUp() {
        samplePercentagePromo = Promotion.builder()
                .code("SUMMER20")
                .name("Ưu đãi mùa hè 20%")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(20))
                .startDate(LocalDate.now().minusDays(5))
                .endDate(LocalDate.now().plusDays(10))
                .usageLimit(100)
                .usedCount(10)
                .minOrderAmount(BigDecimal.valueOf(200000))
                .maxDiscountAmount(BigDecimal.valueOf(100000))
                .status(CommonStatus.ACTIVE)
                .build();
        samplePercentagePromo.setId(1L);

        sampleFixedPromo = Promotion.builder()
                .code("GIAM50K")
                .name("Giảm trực tiếp 50K")
                .discountType(DiscountType.FIXED)
                .discountValue(BigDecimal.valueOf(50000))
                .startDate(LocalDate.now().minusDays(2))
                .endDate(LocalDate.now().plusDays(5))
                .usageLimit(50)
                .usedCount(5)
                .minOrderAmount(BigDecimal.valueOf(150000))
                .status(CommonStatus.ACTIVE)
                .build();
        sampleFixedPromo.setId(2L);
    }

    // =========================================================================
    // 1. DoD VALIDATION & APPLY VOUCHER TESTS (POST /api/vouchers/apply)
    // =========================================================================
    @Nested
    @DisplayName("Tests for applyVoucher (POST /api/vouchers/apply)")
    class ApplyVoucherTests {

        @Test
        @DisplayName("Thành công: Áp dụng mã PERCENTAGE hợp lệ và tính đúng discountAmount, finalAmount")
        void applyVoucher_percentage_success() {
            when(promotionRepository.findByCodeIgnoreCase("SUMMER20"))
                    .thenReturn(Optional.of(samplePercentagePromo));

            VoucherApplyRequestDto request = VoucherApplyRequestDto.builder()
                    .code("SUMMER20")
                    .orderAmount(BigDecimal.valueOf(300000))
                    .build();

            VoucherApplyResponseDto response = promotionService.applyVoucher(request);

            assertThat(response).isNotNull();
            assertThat(response.isValid()).isTrue();
            assertThat(response.getCode()).isEqualTo("SUMMER20");
            assertThat(response.getOrderAmount()).isEqualByComparingTo(BigDecimal.valueOf(300000));
            // 20% của 300,000 = 60,000 (< maxDiscountAmount 100,000)
            assertThat(response.getDiscountAmount()).isEqualByComparingTo(BigDecimal.valueOf(60000));
            assertThat(response.getFinalAmount()).isEqualByComparingTo(BigDecimal.valueOf(240000));
        }

        @Test
        @DisplayName("Thành công: Mã PERCENTAGE đạt trần maxDiscountAmount khi tiền giảm vượt mức tối đa")
        void applyVoucher_percentage_cappedAtMaxDiscount() {
            when(promotionRepository.findByCodeIgnoreCase("SUMMER20"))
                    .thenReturn(Optional.of(samplePercentagePromo));

            // Đơn 1,000,000 -> 20% là 200,000, nhưng trần là 100,000
            VoucherApplyRequestDto request = VoucherApplyRequestDto.builder()
                    .code("SUMMER20")
                    .orderAmount(BigDecimal.valueOf(1000000))
                    .build();

            VoucherApplyResponseDto response = promotionService.applyVoucher(request);

            assertThat(response.getDiscountAmount()).isEqualByComparingTo(BigDecimal.valueOf(100000));
            assertThat(response.getFinalAmount()).isEqualByComparingTo(BigDecimal.valueOf(900000));
        }

        @Test
        @DisplayName("Thành công: Áp dụng mã FIXED giảm số tiền cố định")
        void applyVoucher_fixed_success() {
            when(promotionRepository.findByCodeIgnoreCase("GIAM50K"))
                    .thenReturn(Optional.of(sampleFixedPromo));

            VoucherApplyRequestDto request = VoucherApplyRequestDto.builder()
                    .code("GIAM50K")
                    .orderAmount(BigDecimal.valueOf(200000))
                    .build();

            VoucherApplyResponseDto response = promotionService.applyVoucher(request);

            assertThat(response.isValid()).isTrue();
            assertThat(response.getDiscountAmount()).isEqualByComparingTo(BigDecimal.valueOf(50000));
            assertThat(response.getFinalAmount()).isEqualByComparingTo(BigDecimal.valueOf(150000));
        }

        @Test
        @DisplayName("Thành công: Mã FIXED có giá trị giảm lớn hơn giá trị đơn hàng thì finalAmount = 0 (không âm)")
        void applyVoucher_fixed_discountExceedsOrderAmount() {
            sampleFixedPromo.setMinOrderAmount(BigDecimal.ZERO);
            when(promotionRepository.findByCodeIgnoreCase("GIAM50K"))
                    .thenReturn(Optional.of(sampleFixedPromo));

            VoucherApplyRequestDto request = VoucherApplyRequestDto.builder()
                    .code("GIAM50K")
                    .orderAmount(BigDecimal.valueOf(30000))
                    .build();

            VoucherApplyResponseDto response = promotionService.applyVoucher(request);

            assertThat(response.getDiscountAmount()).isEqualByComparingTo(BigDecimal.valueOf(30000));
            assertThat(response.getFinalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Thất bại: Mã không tồn tại ném ResourceNotFoundException")
        void applyVoucher_codeNotFound() {
            when(promotionRepository.findByCodeIgnoreCase("NOTFOUND"))
                    .thenReturn(Optional.empty());

            VoucherApplyRequestDto request = VoucherApplyRequestDto.builder()
                    .code("NOTFOUND")
                    .orderAmount(BigDecimal.valueOf(500000))
                    .build();

            assertThatThrownBy(() -> promotionService.applyVoucher(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("không tồn tại");
        }

        @Test
        @DisplayName("Thất bại: Mã bị tạm khóa (status = INACTIVE) ném BadRequestException")
        void applyVoucher_inactiveStatus() {
            samplePercentagePromo.setStatus(CommonStatus.INACTIVE);
            when(promotionRepository.findByCodeIgnoreCase("SUMMER20"))
                    .thenReturn(Optional.of(samplePercentagePromo));

            VoucherApplyRequestDto request = VoucherApplyRequestDto.builder()
                    .code("SUMMER20")
                    .orderAmount(BigDecimal.valueOf(500000))
                    .build();

            assertThatThrownBy(() -> promotionService.applyVoucher(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("tạm khóa");
        }

        @Test
        @DisplayName("Thất bại: Mã chưa tới đợt (chưa tới startDate) ném BadRequestException")
        void applyVoucher_upcomingPromo() {
            samplePercentagePromo.setStartDate(LocalDate.now().plusDays(2));
            samplePercentagePromo.setEndDate(LocalDate.now().plusDays(10));
            when(promotionRepository.findByCodeIgnoreCase("SUMMER20"))
                    .thenReturn(Optional.of(samplePercentagePromo));

            VoucherApplyRequestDto request = VoucherApplyRequestDto.builder()
                    .code("SUMMER20")
                    .orderAmount(BigDecimal.valueOf(500000))
                    .build();

            assertThatThrownBy(() -> promotionService.applyVoucher(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("chưa bắt đầu");
        }

        @Test
        @DisplayName("Thất bại: Mã đã hết hạn (sau endDate) ném BadRequestException")
        void applyVoucher_expiredPromo() {
            samplePercentagePromo.setStartDate(LocalDate.now().minusDays(10));
            samplePercentagePromo.setEndDate(LocalDate.now().minusDays(1));
            when(promotionRepository.findByCodeIgnoreCase("SUMMER20"))
                    .thenReturn(Optional.of(samplePercentagePromo));

            VoucherApplyRequestDto request = VoucherApplyRequestDto.builder()
                    .code("SUMMER20")
                    .orderAmount(BigDecimal.valueOf(500000))
                    .build();

            assertThatThrownBy(() -> promotionService.applyVoucher(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("đã hết hạn");
        }

        @Test
        @DisplayName("Thất bại: Quá số lượt dùng tối đa (used_count >= usage_limit) ném BadRequestException")
        void applyVoucher_usageLimitExceeded() {
            samplePercentagePromo.setUsageLimit(50);
            samplePercentagePromo.setUsedCount(50);
            when(promotionRepository.findByCodeIgnoreCase("SUMMER20"))
                    .thenReturn(Optional.of(samplePercentagePromo));

            VoucherApplyRequestDto request = VoucherApplyRequestDto.builder()
                    .code("SUMMER20")
                    .orderAmount(BigDecimal.valueOf(500000))
                    .build();

            assertThatThrownBy(() -> promotionService.applyVoucher(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("hết lượt sử dụng");
        }

        @Test
        @DisplayName("Thất bại: Đơn hàng chưa đạt giá trị tối thiểu (min_order_amount) ném BadRequestException")
        void applyVoucher_orderAmountBelowMinimum() {
            when(promotionRepository.findByCodeIgnoreCase("SUMMER20"))
                    .thenReturn(Optional.of(samplePercentagePromo));

            // minOrderAmount là 200,000 nhưng đơn chỉ 150,000
            VoucherApplyRequestDto request = VoucherApplyRequestDto.builder()
                    .code("SUMMER20")
                    .orderAmount(BigDecimal.valueOf(150000))
                    .build();

            assertThatThrownBy(() -> promotionService.applyVoucher(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("chưa đạt giá trị tối thiểu");
        }
    }

    // =========================================================================
    // 2. CRUD OPERATIONS TESTS (/api/admin/vouchers)
    // =========================================================================
    @Nested
    @DisplayName("Tests for CRUD operations")
    class CrudTests {

        @Test
        @DisplayName("Lấy danh sách voucher phân trang")
        void getPromotions_success() {
            Page<Promotion> page = new PageImpl<>(Collections.singletonList(samplePercentagePromo));
            when(promotionRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

            Page<PromotionDto> result = promotionService.getPromotions(0, 10, "SUMMER", "ACTIVE");

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getCode()).isEqualTo("SUMMER20");
        }

        @Test
        @DisplayName("Lấy chi tiết voucher theo ID thành công")
        void getPromotionById_success() {
            when(promotionRepository.findById(1L)).thenReturn(Optional.of(samplePercentagePromo));

            PromotionDto result = promotionService.getPromotionById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("1");
            assertThat(result.getCode()).isEqualTo("SUMMER20");
        }

        @Test
        @DisplayName("Tạo mới voucher thành công")
        void createPromotion_success() {
            PromotionDto dto = PromotionDto.builder()
                    .code("NEWYEAR2026")
                    .name("Chào năm mới")
                    .discountType("PERCENTAGE")
                    .discountValue(BigDecimal.valueOf(15))
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusMonths(1))
                    .usageLimit(200)
                    .minOrderAmount(BigDecimal.valueOf(100000))
                    .build();

            when(promotionRepository.existsByCodeIgnoreCase("NEWYEAR2026")).thenReturn(false);
            when(promotionRepository.save(any(Promotion.class))).thenAnswer(invocation -> {
                Promotion p = invocation.getArgument(0);
                p.setId(99L);
                return p;
            });

            PromotionDto created = promotionService.createPromotion(dto);

            assertThat(created).isNotNull();
            assertThat(created.getId()).isEqualTo("99");
            assertThat(created.getCode()).isEqualTo("NEWYEAR2026");
        }

        @Test
        @DisplayName("Tạo mới voucher trùng mã ném DuplicateResourceException")
        void createPromotion_duplicateCode() {
            PromotionDto dto = PromotionDto.builder()
                    .code("SUMMER20")
                    .name("Trùng mã")
                    .build();

            when(promotionRepository.existsByCodeIgnoreCase("SUMMER20")).thenReturn(true);

            assertThatThrownBy(() -> promotionService.createPromotion(dto))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("đã tồn tại");
        }

        @Test
        @DisplayName("Tạo mới voucher ngày bắt đầu sau ngày kết thúc ném BadRequestException")
        void createPromotion_invalidDates() {
            PromotionDto dto = PromotionDto.builder()
                    .code("INVALID_DATE")
                    .name("Ngày sai")
                    .discountType("PERCENTAGE")
                    .discountValue(BigDecimal.valueOf(10))
                    .startDate(LocalDate.now().plusDays(10))
                    .endDate(LocalDate.now().plusDays(2))
                    .usageLimit(100)
                    .build();

            when(promotionRepository.existsByCodeIgnoreCase("INVALID_DATE")).thenReturn(false);

            assertThatThrownBy(() -> promotionService.createPromotion(dto))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Ngày bắt đầu không được sau ngày kết thúc");
        }

        @Test
        @DisplayName("Cập nhật voucher thành công")
        void updatePromotion_success() {
            when(promotionRepository.findById(1L)).thenReturn(Optional.of(samplePercentagePromo));
            when(promotionRepository.existsByCodeIgnoreCaseAndIdNot("SUMMER20_UPDATED", 1L)).thenReturn(false);
            when(promotionRepository.save(any(Promotion.class))).thenReturn(samplePercentagePromo);

            PromotionDto dto = PromotionDto.builder()
                    .code("SUMMER20_UPDATED")
                    .name("Tên mới")
                    .discountType("PERCENTAGE")
                    .discountValue(BigDecimal.valueOf(25))
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusDays(20))
                    .usageLimit(150)
                    .build();

            PromotionDto updated = promotionService.updatePromotion(1L, dto);

            assertThat(updated).isNotNull();
            verify(promotionRepository).save(any(Promotion.class));
        }

        @Test
        @DisplayName("Xóa voucher chưa có lượt dùng -> xóa cứng khỏi database")
        void deletePromotion_hardDeleteWhenNoUsage() {
            samplePercentagePromo.setUsedCount(0);
            when(promotionRepository.findById(1L)).thenReturn(Optional.of(samplePercentagePromo));

            promotionService.deletePromotion(1L);

            verify(promotionRepository).delete(samplePercentagePromo);
            verify(promotionRepository, never()).save(any(Promotion.class));
        }

        @Test
        @DisplayName("Xóa voucher đã có lượt dùng -> chuyển status sang INACTIVE (soft-delete)")
        void deletePromotion_softDeleteWhenHasUsage() {
            samplePercentagePromo.setUsedCount(5);
            when(promotionRepository.findById(1L)).thenReturn(Optional.of(samplePercentagePromo));

            promotionService.deletePromotion(1L);

            assertThat(samplePercentagePromo.getStatus()).isEqualTo(CommonStatus.INACTIVE);
            verify(promotionRepository).save(samplePercentagePromo);
            verify(promotionRepository, never()).delete(any(Promotion.class));
        }
    }
}
