package com.example.GYM_management_api.services.impl;

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
import com.example.GYM_management_api.services.IPromotionService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PromotionServiceImpl implements IPromotionService {

    private final PromotionRepository promotionRepository;
    private final PromotionMapper promotionMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<PromotionDto> getPromotions(int page, int size, String keyword, String status) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by(Sort.Direction.DESC, "id"));

        Specification<Promotion> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.trim().isEmpty()) {
                String search = "%" + keyword.trim().toLowerCase() + "%";
                Predicate codePredicate = cb.like(cb.lower(root.get("code")), search);
                Predicate namePredicate = cb.like(cb.lower(root.get("name")), search);
                predicates.add(cb.or(codePredicate, namePredicate));
            }

            if (status != null && !status.trim().isEmpty()) {
                try {
                    CommonStatus statusEnum = CommonStatus.valueOf(status.trim().toUpperCase());
                    predicates.add(cb.equal(root.get("status"), statusEnum));
                } catch (IllegalArgumentException ignored) {}
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return promotionRepository.findAll(spec, pageable).map(promotionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionDto> getAllActivePromotions() {
        return promotionRepository.findByStatus(CommonStatus.ACTIVE).stream()
                .map(promotionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PromotionDto getPromotionById(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã khuyến mãi với ID: " + id));
        return promotionMapper.toDto(promotion);
    }

    @Override
    public PromotionDto createPromotion(PromotionDto request) {
        if (request == null) {
            throw new BadRequestException("Dữ liệu tạo mã khuyến mãi không được để trống.");
        }

        String code = request.getCode() != null ? request.getCode().trim().toUpperCase() : "";
        if (code.isEmpty()) {
            throw new BadRequestException("Mã voucher không được để trống.");
        }

        if (promotionRepository.existsByCodeIgnoreCase(code)) {
            throw new DuplicateResourceException("Mã khuyến mãi '" + code + "' đã tồn tại trong hệ thống.");
        }

        validatePromotionDates(request.getStartDate(), request.getEndDate());
        validatePromotionDiscount(request.getDiscountType(), request.getDiscountValue(), request.getMaxDiscountAmount());

        if (request.getUsageLimit() == null || request.getUsageLimit() < 1) {
            throw new BadRequestException("Giới hạn số lượt dùng tối thiểu là 1.");
        }

        if (request.getMinOrderAmount() != null && request.getMinOrderAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Đơn giá tối thiểu không được là số âm.");
        }

        Promotion entity = promotionMapper.toEntity(request);
        entity.setCode(code);
        entity.setUsedCount(0);

        Promotion saved = promotionRepository.save(entity);
        log.info("Đã tạo mới chiến dịch voucher: ID={}, Code={}", saved.getId(), saved.getCode());
        return promotionMapper.toDto(saved);
    }

    @Override
    public PromotionDto updatePromotion(Long id, PromotionDto request) {
        if (request == null) {
            throw new BadRequestException("Dữ liệu cập nhật không được để trống.");
        }

        Promotion existing = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã khuyến mãi với ID: " + id));

        String code = request.getCode() != null ? request.getCode().trim().toUpperCase() : "";
        if (code.isEmpty()) {
            throw new BadRequestException("Mã voucher không được để trống.");
        }

        if (promotionRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw new DuplicateResourceException("Mã khuyến mãi '" + code + "' đã được sử dụng bởi chiến dịch khác.");
        }

        validatePromotionDates(request.getStartDate(), request.getEndDate());
        validatePromotionDiscount(request.getDiscountType(), request.getDiscountValue(), request.getMaxDiscountAmount());

        if (request.getUsageLimit() != null && request.getUsageLimit() < existing.getUsedCount()) {
            throw new BadRequestException("Giới hạn lượt dùng (" + request.getUsageLimit()
                    + ") không được nhỏ hơn số lượt đã sử dụng hiện tại (" + existing.getUsedCount() + ").");
        }

        if (request.getMinOrderAmount() != null && request.getMinOrderAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Đơn giá tối thiểu không được là số âm.");
        }

        promotionMapper.updateEntityFromDto(request, existing);
        existing.setCode(code);

        Promotion updated = promotionRepository.save(existing);
        log.info("Đã cập nhật chiến dịch voucher: ID={}, Code={}", updated.getId(), updated.getCode());
        return promotionMapper.toDto(updated);
    }

    @Override
    public void deletePromotion(Long id) {
        Promotion existing = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã khuyến mãi với ID: " + id));

        // Nếu mã đã được sử dụng, thực hiện vô hiệu hóa (soft delete) để bảo toàn lịch sử giao dịch
        if (existing.getUsedCount() != null && existing.getUsedCount() > 0) {
            existing.setStatus(CommonStatus.INACTIVE);
            promotionRepository.save(existing);
            log.info("Chiến dịch voucher ID={} đã phát sinh giao dịch, chuyển trạng thái sang INACTIVE", id);
        } else {
            promotionRepository.delete(existing);
            log.info("Đã xóa hoàn toàn chiến dịch voucher ID={}", id);
        }
    }

    @Override
    @Transactional
    public VoucherApplyResponseDto applyVoucher(VoucherApplyRequestDto request) {
        if (request == null || request.getCode() == null || request.getCode().trim().isEmpty()) {
            throw new BadRequestException("Mã voucher không được để trống.");
        }

        BigDecimal orderAmount = request.getOrderAmount();
        if (orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Giá trị đơn hàng không được là số âm.");
        }

        String searchCode = request.getCode().trim().toUpperCase();

        // 1. Kiểm tra mã tồn tại
        Promotion promo = promotionRepository.findByCodeIgnoreCase(searchCode)
                .orElseThrow(() -> new ResourceNotFoundException("Mã giảm giá '" + searchCode + "' không tồn tại trong hệ thống."));

        // 2. Kiểm tra trạng thái hoạt động
        if (promo.getStatus() != CommonStatus.ACTIVE) {
            throw new BadRequestException("Mã giảm giá '" + promo.getCode() + "' hiện đang bị tạm khóa hoặc đã ngưng áp dụng.");
        }

        // 3. Kiểm tra hạn dùng
        LocalDate today = LocalDate.now();
        if (promo.getStartDate() != null && today.isBefore(promo.getStartDate())) {
            throw new BadRequestException("Chương trình ưu đãi cho mã '" + promo.getCode()
                    + "' chưa bắt đầu (Có hiệu lực từ ngày: " + promo.getStartDate() + ").");
        }
        if (promo.getEndDate() != null && today.isAfter(promo.getEndDate())) {
            throw new BadRequestException("Mã giảm giá '" + promo.getCode()
                    + "' đã hết hạn sử dụng (Hết hạn từ ngày: " + promo.getEndDate() + ").");
        }

        // 4. Kiểm tra số lượt dùng tối đa
        int usedCount = promo.getUsedCount() != null ? promo.getUsedCount() : 0;
        int usageLimit = promo.getUsageLimit() != null ? promo.getUsageLimit() : 0;
        if (usedCount >= usageLimit) {
            throw new BadRequestException("Mã giảm giá '" + promo.getCode() + "' đã hết lượt sử dụng.");
        }

        // 5. Kiểm tra đơn giá tối thiểu
        BigDecimal minOrderAmount = promo.getMinOrderAmount() != null ? promo.getMinOrderAmount() : BigDecimal.ZERO;
        if (orderAmount.compareTo(minOrderAmount) < 0) {
            throw new BadRequestException("Đơn hàng chưa đạt giá trị tối thiểu " + minOrderAmount
                    + " VND để áp dụng mã giảm giá này.");
        }

        // 6. Tính mức giảm theo % hoặc số tiền cố định
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (promo.getDiscountType() == DiscountType.PERCENTAGE) {
            discountAmount = orderAmount.multiply(promo.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            if (promo.getMaxDiscountAmount() != null && discountAmount.compareTo(promo.getMaxDiscountAmount()) > 0) {
                discountAmount = promo.getMaxDiscountAmount();
            }
        } else if (promo.getDiscountType() == DiscountType.FIXED) {
            discountAmount = promo.getDiscountValue();
        }

        // Không để tiền giảm vượt quá giá trị đơn hàng
        if (discountAmount.compareTo(orderAmount) > 0) {
            discountAmount = orderAmount;
        }

        BigDecimal finalAmount = orderAmount.subtract(discountAmount);

        // 7. Cập nhật tăng số lượt sử dụng voucher (+1)
        int newUsedCount = usedCount + 1;
        promo.setUsedCount(newUsedCount);
        promotionRepository.save(promo);

        log.info("Áp dụng mã voucher '{}' thành công: Giá gốc={}, Giảm={}, Còn lại={}, Lượt dùng={}/{}",
                promo.getCode(), orderAmount, discountAmount, finalAmount, newUsedCount, usageLimit);

        return VoucherApplyResponseDto.builder()
                .valid(true)
                .code(promo.getCode())
                .name(promo.getName())
                .discountType(promo.getDiscountType() != null ? promo.getDiscountType().name() : "PERCENTAGE")
                .discountValue(promo.getDiscountValue())
                .orderAmount(orderAmount)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .usedCount(newUsedCount)
                .usageLimit(usageLimit)
                .message("Áp dụng mã giảm giá thành công.")
                .build();
    }

    private void validatePromotionDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            throw new BadRequestException("Ngày bắt đầu không được để trống.");
        }
        if (endDate == null) {
            throw new BadRequestException("Ngày kết thúc không được để trống.");
        }
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("Ngày bắt đầu không được sau ngày kết thúc.");
        }
    }

    private void validatePromotionDiscount(String discountTypeStr, BigDecimal discountValue, BigDecimal maxDiscountAmount) {
        if (discountValue == null || discountValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Giá trị giảm giá phải lớn hơn 0.");
        }

        DiscountType discountType = DiscountType.PERCENTAGE;
        if (discountTypeStr != null) {
            try {
                discountType = DiscountType.valueOf(discountTypeStr.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Loại giảm giá không hợp lệ (phải là PERCENTAGE hoặc FIXED).");
            }
        }

        if (discountType == DiscountType.PERCENTAGE) {
            if (discountValue.compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new BadRequestException("Phần trăm giảm giá không được vượt quá 100%.");
            }
            if (maxDiscountAmount != null && maxDiscountAmount.compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Mức giảm tối đa không được là số âm.");
            }
        }
    }
}
