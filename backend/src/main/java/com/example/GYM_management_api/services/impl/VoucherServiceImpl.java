package com.example.GYM_management_api.services.impl;

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
import com.example.GYM_management_api.services.IVoucherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class VoucherServiceImpl implements IVoucherService {

    private final PromotionRepository promotionRepository;
    private final MembershipRepository membershipRepository;

    @Override
    public VoucherValidateResponseDto validateVoucher(VoucherValidateRequestDto request) {
        if (request == null || request.getCode() == null || request.getCode().trim().isEmpty()) {
            throw new BadRequestException("Mã voucher không được để trống.");
        }

        return validateVoucherInternal(
                request.getCode(),
                request.getMembershipId(),
                request.getOrderAmount(),
                request.getMemberId()
        );
    }

    @Override
    public VoucherValidateResponseDto validateVoucherInternal(
            String code,
            Long membershipId,
            BigDecimal orderAmount,
            Long memberId) {

        if (code == null || code.trim().isEmpty()) {
            throw new BadRequestException("Mã voucher không được để trống.");
        }

        String searchCode = code.trim().toUpperCase();

        // 1. Tìm voucher trong database
        Promotion promo = promotionRepository.findByCodeIgnoreCase(searchCode)
                .orElseThrow(() -> new ResourceNotFoundException("Mã voucher '" + searchCode + "' không tồn tại trong hệ thống."));

        // 2. Kiểm tra trạng thái hoạt động
        if (promo.getStatus() != CommonStatus.ACTIVE) {
            throw new BadRequestException("Mã voucher '" + promo.getCode() + "' hiện đang bị tạm khóa hoặc đã ngưng áp dụng.");
        }

        // 3. Kiểm tra hạn dùng
        LocalDate today = LocalDate.now();
        if (promo.getStartDate() != null && today.isBefore(promo.getStartDate())) {
            throw new BadRequestException("Chương trình ưu đãi cho mã '" + promo.getCode()
                    + "' chưa bắt đầu (Có hiệu lực từ ngày: " + promo.getStartDate() + ").");
        }
        if (promo.getEndDate() != null && today.isAfter(promo.getEndDate())) {
            throw new BadRequestException("Mã voucher '" + promo.getCode()
                    + "' đã hết hạn sử dụng (Hạn dùng đến ngày: " + promo.getEndDate() + ").");
        }

        // 4. Kiểm tra số lượt dùng tối đa
        int usedCount = promo.getUsedCount() != null ? promo.getUsedCount() : 0;
        int usageLimit = promo.getUsageLimit() != null ? promo.getUsageLimit() : 0;
        if (usedCount >= usageLimit) {
            throw new BadRequestException("Mã voucher '" + promo.getCode() + "' đã hết lượt sử dụng.");
        }

        // 5. Kiểm tra gói tập áp dụng nếu có điều kiện
        if (Boolean.FALSE.equals(promo.getApplicableToAll())
                && promo.getApplicableMembershipIds() != null
                && !promo.getApplicableMembershipIds().isBlank()) {
            if (membershipId != null) {
                Set<Long> allowedIds = Arrays.stream(promo.getApplicableMembershipIds().split("[,;\\s]+"))
                        .filter(s -> !s.isBlank())
                        .map(String::trim)
                        .map(Long::parseLong)
                        .collect(Collectors.toSet());
                if (!allowedIds.contains(membershipId)) {
                    throw new BadRequestException("Mã voucher '" + promo.getCode() + "' không áp dụng cho gói tập đã chọn.");
                }
            }
        }

        // 6. Xác định giá trị đơn hàng nếu chưa được truyền
        BigDecimal effectiveOrderAmount = orderAmount;
        if (effectiveOrderAmount == null || effectiveOrderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            if (membershipId != null) {
                Membership membership = membershipRepository.findById(membershipId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy gói tập với ID: " + membershipId));
                effectiveOrderAmount = membership.getPrice();
            } else {
                effectiveOrderAmount = BigDecimal.ZERO;
            }
        }

        // 7. Kiểm tra giá trị đơn hàng tối thiểu
        BigDecimal minOrderAmount = promo.getMinOrderAmount() != null ? promo.getMinOrderAmount() : BigDecimal.ZERO;
        if (effectiveOrderAmount.compareTo(minOrderAmount) < 0) {
            throw new BadRequestException("Đơn hàng chưa đạt giá trị tối thiểu " + minOrderAmount
                    + " VND để áp dụng mã voucher này.");
        }

        // 8. Tính số tiền giảm giá
        BigDecimal discountAmount = BigDecimal.ZERO;
        DiscountType discountType = promo.getDiscountType() != null ? promo.getDiscountType() : DiscountType.PERCENTAGE;

        if (discountType == DiscountType.PERCENTAGE || discountType == DiscountType.PERCENT) {
            BigDecimal value = promo.getDiscountValue() != null ? promo.getDiscountValue() : BigDecimal.ZERO;
            discountAmount = effectiveOrderAmount.multiply(value)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            if (promo.getMaxDiscountAmount() != null && discountAmount.compareTo(promo.getMaxDiscountAmount()) > 0) {
                discountAmount = promo.getMaxDiscountAmount();
            }
        } else if (discountType == DiscountType.FIXED) {
            discountAmount = promo.getDiscountValue() != null ? promo.getDiscountValue() : BigDecimal.ZERO;
        } else if (discountType == DiscountType.FREE) {
            discountAmount = effectiveOrderAmount;
        }

        // Không cho phép số tiền giảm vượt quá giá trị đơn hàng
        if (discountAmount.compareTo(effectiveOrderAmount) > 0) {
            discountAmount = effectiveOrderAmount;
        }

        BigDecimal finalPrice = effectiveOrderAmount.subtract(discountAmount);

        log.debug("Kiểm tra mã voucher '{}': Giá gốc={}, Giảm={}, Còn lại={}",
                promo.getCode(), effectiveOrderAmount, discountAmount, finalPrice);

        return VoucherValidateResponseDto.builder()
                .valid(true)
                .code(promo.getCode())
                .name(promo.getName())
                .discountType(discountType.name())
                .discountValue(promo.getDiscountValue())
                .minOrderValue(minOrderAmount)
                .maxDiscount(promo.getMaxDiscountAmount())
                .applicableToAll(promo.getApplicableToAll() != null ? promo.getApplicableToAll() : true)
                .applicableMembershipIds(promo.getApplicableMembershipIds())
                .expiredAt(promo.getEndDate())
                .status(promo.getStatus() != null ? promo.getStatus().name() : "ACTIVE")
                .orderAmount(effectiveOrderAmount)
                .discountAmount(discountAmount)
                .amountAfterDiscount(finalPrice)
                .finalPrice(finalPrice)
                .message("Áp dụng mã giảm giá thành công.")
                .build();
    }
}
